package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.operation.CancelAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.library.serializer.moneyFormatter
import com.bookk.server.appointments.client.api.event.AppointmentEvent
import library.permissions.ObjectPermission
import library.permissions.assert
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

private val declineAppointmentRequestLogger = LoggerFactory.getLogger(DeclineAppointmentImpl::class.java)

internal class DeclineAppointmentImpl(
    private val appointmentDataSource: AppointmentDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val eventProducer: StandardEventProducer,
    private val transactionManager: TransactionManager
) : CancelAppointment {
    override suspend fun invoke(userId: Uuid, cancellation: AppointmentCancellation): Result<Unit> = transactionManager.transaction {
        permissionsDataSource.getPermissions(userId, cancellation.businessId).assert(ObjectPermission.EDIT)
        val appointment = appointmentDataSource.cancel(cancellation.id, cancellation.reason)
        sendAppointmentCancelledEvent(appointment)
    }

    private suspend fun sendAppointmentCancelledEvent(appointment: Appointment) {
        val business = subscriptionDataSource.getBusinessSnapshot(appointment.businessId) ?: run {
            declineAppointmentRequestLogger.error("No business with id ${appointment.businessId} exists")
            return
        }
        eventProducer.send(
            AppointmentEvent.Cancelled(
                from = appointment.date,
                to = appointment.date + appointment.service.duration,
                businessName = business.name,
                executioner = "TODO",
                address = business.address,
                price = moneyFormatter.print(appointment.service.price),
                reason = appointment.cancellationReason
            )
        )
    }
}