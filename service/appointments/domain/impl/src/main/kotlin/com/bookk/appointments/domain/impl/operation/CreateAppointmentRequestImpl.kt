package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
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
import library.permissions.ObjectPermission
import library.permissions.assert
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

private val createAppointmentRequestLogger = LoggerFactory.getLogger(CreateAppointmentRequestImpl::class.java)


internal class CreateAppointmentRequestImpl(
    private val requestDataSource: AppointmentRequestDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val eventProducer: StandardEventProducer,
    private val createAppointment: CreateAppointment,
    private val transactionManager: TransactionManager
) : CreateAppointmentRequest {

    override suspend fun invoke(
        userId: Uuid,
        request: AppointmentRequest
    ): Result<Unit> = transactionManager.transaction {
        val settings = settingsDataSource.getForUpdate(request.businessId) ?: throw Error.NotFound()
        permissionsDataSource.getPermissions(userId, request.businessId).assert(ObjectPermission.EDIT)
        if (settings.automaticApproval) {
            return@transaction createAppointment(userId, request)
                .map { Unit }
                .getOrThrow()
        }
        if (settings.isInWorkday(request.date)) throw CreateAppointmentRequest.Error.RequestForThisDateNotAllowed()
        if (settings.isInWorktime(request.date)) throw CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed()
        if (requestDataSource.hasOverlapsWith(request)) throw CreateAppointmentRequest.Error.RequestForThisTimeExists()
        requestDataSource.create(request).also {
            sendRequestCreatedNotification(request)
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