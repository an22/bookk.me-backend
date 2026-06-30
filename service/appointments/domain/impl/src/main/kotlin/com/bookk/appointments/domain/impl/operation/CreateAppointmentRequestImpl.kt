package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentOffer
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest.Error.DateInThePastNotAllowed
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
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import com.bookk.library.serializer.moneyFormatter
import com.bookk.server.appointments.client.api.event.AppointmentEvent
import com.bookk.server.business.client.api.QuoteClaims
import library.permissions.ObjectPermission
import library.permissions.assert
import library.signing.TokenValidatorFactory
import library.signing.ValidationType
import org.joda.money.Money
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.uuid.Uuid

private val createAppointmentRequestLogger = LoggerFactory.getLogger(CreateAppointmentRequestImpl::class.java)


internal class CreateAppointmentRequestImpl(
    private val requestDataSource: AppointmentRequestDataSource,
    private val appointmentDataSource: AppointmentDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val eventProducer: StandardEventProducer,
    private val createAppointment: CreateAppointment,
    private val transactionManager: TransactionManager,
    private val tokenValidatorFactory: TokenValidatorFactory
) : CreateAppointmentRequest {

    override suspend fun invoke(userId: Uuid, offer: AppointmentOffer): Result<Unit> {
        val request = offer.request

        if (requestDataSource.isTokenInCache(offer.offerToken)) return Result.failure(TokenAlreadyUsed())
        val decodedOffer = runCatching {
            tokenValidatorFactory.forType(ValidationType.SERVICE_QUOTE)
                .verifier
                .verify(offer.offerToken)
        }.getOrElse { return Result.failure(ServicesSignatureMiss()) }

        val services = decodedOffer.getClaim(QuoteClaims.CLAIM_SERVICES).asList(String::class.java).toSet()
        val total = decodedOffer.getClaim(QuoteClaims.CLAIM_TOTAL).asString()
        val businessId = decodedOffer.getClaim(QuoteClaims.CLAIM_BUSINESS_ID).asString()

        if (request.totalAmount != Money.parse(total)) return Result.failure(PriceChanged())
        if (request.services.map { it.id.toString() }.toSet() != services) return Result.failure(ServicesSignatureMiss())
        if (request.businessId.toString() != businessId) return Result.failure(ServicesSignatureMiss())

        return transactionManager.transaction<Unit> {
            val settings = settingsDataSource.getForUpdate(request.businessId) ?: throw Error.NotFound()
            permissionsDataSource.getPermissions(userId, request.businessId).assert(ObjectPermission.EDIT)

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
            requestDataSource.cacheOfferToken(offer.offerToken)
        }
    }

    private suspend fun sendRequestCreatedNotification(request: AppointmentRequest) {
        val business = subscriptionDataSource.getBusinessSnapshot(request.businessId) ?: run {
            createAppointmentRequestLogger.error("No business with id ${request.businessId} exists")
            throw Error.NotFound()
        }
        eventProducer.send(
            AppointmentEvent.RequestCreated(
                from = request.date,
                to = request.dateEnd,
                businessName = business.name,
                executioner = "TODO",
                address = business.address,
                price = moneyFormatter.print(request.totalAmount)
            )
        )
    }
}