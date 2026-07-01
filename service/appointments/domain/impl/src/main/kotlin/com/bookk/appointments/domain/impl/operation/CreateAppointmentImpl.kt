package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRepresentation
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
import kotlin.time.Clock
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

    override suspend fun invoke(userId: Uuid, appointment: Appointment, isInstant: Boolean): Result<Appointment> {
        if (isInstant && userId != appointment.userId) return Result.failure(CreateAppointment.Error.InstantAppointmentOnlySelfAllowed())
        return transactionManager.transaction {
            createAppointment(userId, appointment)
        }
    }

    private suspend fun createAppointment(userId: Uuid, appointment: Appointment): Appointment {
        verifyAppointment(userId, appointment)
        return appointmentDataSource.create(appointment)
    }

    private suspend fun createAppointment(userId: Uuid, request: AppointmentRequest): Appointment {
        verifyAppointment(userId, request)
        return appointmentDataSource.create(request).also {
            requestDataSource.approve(request)
            sendRequestApprovedNotification(request)
        }
    }

    private suspend fun verifyAppointment(userId: Uuid, appointment: AppointmentRepresentation) {
        val settings = settingsDataSource.getForUpdate(appointment.businessId) ?: throw Error.NotFound()
        permissionsDataSource.getPermissions(userId, appointment.businessId).assert(ObjectPermission.EDIT)
        if (appointment.date < Clock.System.now()) throw CreateAppointment.Error.DateInThePastNotAllowed()
        if (!settings.isInWorkday(appointment.date)) throw CreateAppointment.Error.RequestForThisDateNotAllowed()
        if (!settings.isInWorktime(appointment.date, appointment.dateEnd)) throw CreateAppointment.Error.RequestForThisTimeNotAllowed()
        if (appointmentDataSource.hasOverlapsWith(appointment)) throw CreateAppointment.Error.AppointmentForThisTimeExists()
    }

    private suspend fun sendRequestApprovedNotification(request: AppointmentRequest) {
        val business = subscriptionDataSource.getBusinessSnapshot(request.businessId) ?: run {
            createAppointmentLogger.error("No business with id ${request.businessId} exists")
            throw Error.NotFound()
        }
        eventProducer.send(
            AppointmentEvent.RequestApproved(
                from = request.date,
                to = request.dateEnd,
                businessName = business.name,
                executioner = request.employee.fullName,
                address = business.address,
                price = moneyFormatter.print(request.totalAmount)
            )
        )
    }
}
