package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.operation.CompleteAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.PermissionAction
import library.permissions.assertOrSelf
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class CompleteAppointmentImpl(
    private val appointmentDataSource: AppointmentDataSource,
    private val appointmentPermissionDataSource: AppointmentPermissionDataSource,
    private val transactionManager: TransactionManager
) : CompleteAppointment {

    override suspend fun invoke(userId: Uuid, appointmentId: Uuid): Result<Appointment> = transactionManager.transaction {
        val appointment = appointmentDataSource.get(appointmentId)
        appointmentPermissionDataSource.getPermission(userId, appointment.businessId)
            .assertOrSelf(PermissionAction.UPDATE, actorId = userId, assigneeId = appointment.employee.userId)
        val completed = appointmentDataSource.markCompletedByUser(appointmentId, startedBefore = Clock.System.now())
        when (completed.status) {
            AppointmentStatus.COMPLETED -> completed
            AppointmentStatus.CANCELLED -> throw CompleteAppointment.Error.AlreadyCancelled()
            AppointmentStatus.NO_SHOW -> throw CompleteAppointment.Error.MarkedNoShow()
            AppointmentStatus.SCHEDULED -> throw CompleteAppointment.Error.NotStarted()
        }
    }
}
