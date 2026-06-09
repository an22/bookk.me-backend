package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.operation.GetAppointments
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class GetAppointmentsImpl(
    private val appointmentDataSource: AppointmentDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val transactionManager: TransactionManager
) : GetAppointments {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<List<Appointment>> = transactionManager.transaction {
        permissionsDataSource.getPermissions(userId, businessId).assert(ObjectPermission.READ)
        appointmentDataSource.getAll(businessId)
    }
}