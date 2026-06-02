package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class CreateAppointmentImpl(
    private val appointmentDataSource: AppointmentDataSource,
    private val requestDataSource: AppointmentRequestDataSource,
    private val settingsDataSource: AppointmentSettingsDataSource,
    private val transactionManager: TransactionManager
) : CreateAppointment {
    override suspend fun invoke(
        userId: Uuid,
        appointmentRequestId: Uuid
    ): Result<Appointment> = transactionManager.transaction {
        val request = requestDataSource.get(appointmentRequestId) ?: throw Error.NotFound()
        val settings = settingsDataSource.getForUpdate(request.businessId) ?: throw Error.NotFound()
        appointmentDataSource.getPermissions(userId, request.businessId).assert(ObjectPermission.WRITE)
        if (!settings.isInWorkday(request.date)) throw CreateAppointment.Error.RequestForThisDateNotAllowed()
        if (!settings.isInWorktime(request.date)) throw CreateAppointment.Error.RequestForThisTimeNotAllowed()
        if (appointmentDataSource.hasOverlapsWith(request)) throw CreateAppointment.Error.RequestForThisTimeExists()
        appointmentDataSource.create(request)
    }
}