package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentRequestDraft
import com.bookk.appointments.domain.api.entity.AppointmentRequestStatus
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.EmployeeSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.DateInThePastNotAllowed
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.DurationChanged
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.PriceChanged
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.RequestForThisDateNotAllowed
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.RequestForThisTimeExists
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.ServicesSignatureMiss
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.TokenAlreadyUsed
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import com.bookk.library.serializer.moneyFormatter
import com.bookk.server.appointments.client.api.event.AppointmentEvent
import com.bookk.server.business.client.api.BusinessClient
import com.bookk.server.business.client.api.QuoteClaims
import library.signing.TokenValidatorFactory
import library.signing.ValidationType
import org.joda.money.Money
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.uuid.Uuid

private val createAppointmentRequestLogger = LoggerFactory.getLogger(CreateAppointmentRequestImpl::class.java)


internal class CreateAppointmentRequestImpl(
    private val requestDataSource: AppointmentRequestDataSource,
    private val appointmentDataSource: AppointmentDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val eventProducer: StandardEventProducer,
    private val createAppointment: CreateAppointment,
    private val transactionManager: TransactionManager,
    private val tokenValidatorFactory: TokenValidatorFactory,
    private val businessClient: BusinessClient
) : CreateAppointmentRequest {

    override suspend fun invoke(userId: Uuid, draft: AppointmentRequestDraft): Result<Unit> {
        if (requestDataSource.isTokenInCache(draft.offerToken)) return Result.failure(TokenAlreadyUsed())
        if (draft.services.any { it.count <= 0 }) return Result.failure(ServicesSignatureMiss())
        val requestedServiceIds = draft.services.map { it.serviceId }
        if (requestedServiceIds.distinct().size != requestedServiceIds.size) return Result.failure(ServicesSignatureMiss())
        val decodedOffer = runCatching {
            tokenValidatorFactory.forType(ValidationType.SERVICE_QUOTE)
                .verifier
                .verify(draft.offerToken)
        }.getOrElse { return Result.failure(ServicesSignatureMiss()) }

        val claimedServiceCounts = QuoteClaims.decodeServiceCounts(decodedOffer.getClaim(QuoteClaims.CLAIM_SERVICES).asList(String::class.java))
        val total = decodedOffer.getClaim(QuoteClaims.CLAIM_TOTAL).asString()
        val totalDuration = decodedOffer.getClaim(QuoteClaims.CLAIM_DURATION).asString()
        val businessId = decodedOffer.getClaim(QuoteClaims.CLAIM_BUSINESS_ID).asString()

        val requestedServiceCounts = draft.services.associate { it.serviceId to it.count }
        if (requestedServiceCounts != claimedServiceCounts) return Result.failure(ServicesSignatureMiss())
        if (draft.businessId.toString() != businessId) return Result.failure(ServicesSignatureMiss())

        val context = businessClient.getAppointmentBookingContext(draft.businessId, draft.employeeId, userId, requestedServiceIds)
            .getOrElse { return Result.failure(it) }

        val servicesById = context.services.associateBy { it.id }
        val expandedServices = draft.services.flatMap { requested -> List(requested.count) { servicesById.getValue(requested.serviceId) } }

        val request = AppointmentRequest(
            id = Uuid.random(),
            userId = userId,
            businessId = draft.businessId,
            employee = EmployeeSnapshot(
                id = context.employee.id,
                userId = context.employee.userId,
                fullName = "${context.employee.name} ${context.employee.lastName}".trim()
            ),
            client = ClientSnapshot(
                id = context.client.id,
                fullName = "${context.client.name} ${context.client.lastName}".trim(),
                phone = context.client.phone.orEmpty(),
                email = context.client.email.orEmpty()
            ),
            services = expandedServices.map {
                ServiceSnapshot(id = it.id, name = it.name, groupId = it.group.id, price = it.price, duration = it.duration)
            },
            status = AppointmentRequestStatus.PENDING,
            date = draft.date,
            note = draft.note,
            declineReason = ""
        )

        if (request.totalAmount != Money.parse(total)) return Result.failure(PriceChanged())
        if (request.dateEnd - request.date != Duration.parse(totalDuration)) return Result.failure(DurationChanged())

        return transactionManager.transaction<Unit> {
            val settings = settingsDataSource.getForUpdate(request.businessId) ?: throw Error.NotFound()

            if (settings.automaticApproval) {
                return@transaction createAppointment(userId, request)
                    .map { Unit }
                    .getOrThrow()
            }
            if (request.date < Clock.System.now()) throw DateInThePastNotAllowed()
            if (!settings.isInWorkday(request.date)) throw RequestForThisDateNotAllowed()
            if (!settings.isInWorktime(request.date, request.dateEnd)) throw RequestForThisTimeNotAllowed()
            if (requestDataSource.hasOverlapsWith(request)) throw RequestForThisTimeExists()
            if (appointmentDataSource.hasOverlapsWith(request)) throw RequestForThisTimeExists()

            requestDataSource.create(request).also {
                sendRequestCreatedNotification(request)
            }
        }.onSuccess {
            requestDataSource.cacheOfferToken(draft.offerToken)
        }
    }

    private suspend fun sendRequestCreatedNotification(request: AppointmentRequest) {
        val business = subscriptionDataSource.getBusinessSnapshot(request.businessId) ?: run {
            createAppointmentRequestLogger.error("No business with id ${request.businessId} exists")
            throw Error.NotFound()
        }
        eventProducer.send(
            AppointmentEvent.RequestCreated(
                clientUserId = request.client.id,
                clientName = request.client.fullName,
                employeeUserId = request.employee.userId,
                employeeName = request.employee.fullName,
                from = request.date,
                to = request.dateEnd,
                timeZone = business.timeZone,
                businessName = business.name,
                address = business.address,
                price = moneyFormatter.print(request.totalAmount)
            )
        )
    }
}