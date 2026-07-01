package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.operation.CancelAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
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

private val declineAppointmentLogger = LoggerFactory.getLogger(CancelAppointmentImpl::class.java)

internal class CancelAppointmentImpl(
    private val appointmentDataSource: AppointmentDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val eventProducer: StandardEventProducer,
    private val transactionManager: TransactionManager
) : CancelAppointment {

    override suspend fun invoke(userId: Uuid, cancellation: AppointmentCancellation): Result<Appointment> = transactionManager.transaction {
        permissionsDataSource.getPermissions(userId, cancellation.businessId).assert(ObjectPermission.EDIT)
        val appointment = appointmentDataSource.get(cancellation.id)
        val cancelled = when (appointment.status) {
            AppointmentStatus.COMPLETED -> throw CancelAppointment.Error.AlreadyCompleted()
            AppointmentStatus.CANCELLED -> throw CancelAppointment.Error.AlreadyCancelled()
            AppointmentStatus.SCHEDULED -> appointmentDataSource.cancel(cancellation.id, cancellation.reason)
        }
        sendAppointmentCancelledEvent(cancelled)
        appointment.copy(
            status = AppointmentStatus.CANCELLED,
            cancellationReason = cancellation.reason
        )
    }

    private suspend fun sendAppointmentCancelledEvent(appointment: Appointment) {
        val business = subscriptionDataSource.getBusinessSnapshot(appointment.businessId) ?: run {
            declineAppointmentLogger.error("No business with id ${appointment.businessId} exists")
            throw Error.NotFound()
        }
        eventProducer.send(
            AppointmentEvent.Cancelled(
                from = appointment.date,
                to = appointment.dateEnd,
                businessName = business.name,
                executioner = appointment.employee.fullName,
                address = business.address,
                price = moneyFormatter.print(appointment.totalAmount),
                reason = appointment.cancellationReason
            )
        )
    }
}