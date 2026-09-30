package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.operation.UpdateAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import library.permissions.PermissionAction
import library.permissions.assertOrSelf
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class UpdateAppointmentImpl(
    private val appointmentDataSource: AppointmentDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val appointmentPermissionDataSource: AppointmentPermissionDataSource,
    private val transactionManager: TransactionManager
) : UpdateAppointment {
    override suspend fun invoke(userId: Uuid, appointment: Appointment): Result<Appointment> {
        if (appointment.status != AppointmentStatus.SCHEDULED) return Result.failure(UpdateAppointment.Error.StatusChangeNotAllowed())
        return transactionManager.transaction {
            val settings = settingsDataSource.getForUpdate(appointment.businessId) ?: throw Error.NotFound()
            val existing = appointmentDataSource.getForUpdate(appointment.id)
            appointmentPermissionDataSource.getPermission(userId, appointment.businessId)
                .assertOrSelf(PermissionAction.UPDATE, actorId = userId, assigneeId = existing.employee.userId)
            existing.requireScheduled()
            if (appointment.date < Clock.System.now()) throw UpdateAppointment.Error.DateInThePastNotAllowed()
            if (!settings.isInWorkday(appointment.date)) throw UpdateAppointment.Error.RequestForThisDateNotAllowed()
            if (!settings.isInWorktime(appointment.date, appointment.dateEnd)) throw UpdateAppointment.Error.RequestForThisTimeNotAllowed()
            if (appointmentDataSource.hasOverlapsWith(appointment)) throw UpdateAppointment.Error.AppointmentForThisTimeExists()
            appointmentDataSource.update(appointment)
        }
    }

    private fun Appointment.requireScheduled() = when (status) {
        AppointmentStatus.SCHEDULED -> Unit
        AppointmentStatus.CANCELLED -> throw UpdateAppointment.Error.AlreadyCancelled()
        AppointmentStatus.COMPLETED -> throw UpdateAppointment.Error.AlreadyCompleted()
        AppointmentStatus.NO_SHOW -> throw UpdateAppointment.Error.MarkedNoShow()
    }
}