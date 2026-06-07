package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointment
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
import library.permissions.ObjectPermission
import library.permissions.assert
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

private val createAppointmentLogger = LoggerFactory.getLogger(CreateAppointmentImpl::class.java)

internal class CreateAppointmentImpl(
    private val appointmentDataSource: AppointmentDataSource,
    private val requestDataSource: AppointmentRequestDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : CreateAppointment {

    override suspend fun invoke(
        userId: Uuid,
        appointmentRequestId: Uuid
    ): Result<Appointment> = transactionManager.transaction {
        val request = requestDataSource.get(appointmentRequestId) ?: throw Error.NotFound()
        createAppointment(userId, request)
    }

    override suspend fun invoke(userId: Uuid, request: AppointmentRequest): Result<Appointment> =
        transactionManager.transaction {
            createAppointment(userId, request)
        }

    private suspend fun createAppointment(userId: Uuid, request: AppointmentRequest): Appointment {
        val settings = settingsDataSource.getForUpdate(request.businessId) ?: throw Error.NotFound()
        permissionsDataSource.getPermissions(userId, request.businessId).assert(ObjectPermission.EDIT)
        if (settings.isInWorkday(request.date)) throw CreateAppointment.Error.RequestForThisDateNotAllowed()
        if (settings.isInWorktime(request.date)) throw CreateAppointment.Error.RequestForThisTimeNotAllowed()
        if (appointmentDataSource.hasOverlapsWith(request)) throw CreateAppointment.Error.AppointmentForThisTimeExists()
        return appointmentDataSource.create(request).also {
            requestDataSource.approve(request)
            sendRequestApprovedNotification(request)
        }
    }

    private suspend fun sendRequestApprovedNotification(request: AppointmentRequest) {
        val business = subscriptionDataSource.getBusinessSnapshot(request.businessId) ?: run {
            createAppointmentLogger.error("No business with id ${request.businessId} exists")
            return
        }
        eventProducer.send(
            AppointmentEvent.RequestApproved(
                from = request.date,
                to = request.date + request.service.duration,
                businessName = business.name,
                executioner = "TODO",
                address = business.address,
                price = moneyFormatter.print(request.service.price)
            )
        )
    }
}
