package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.operation.GetAppointmentHistory
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Pagination
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class GetAppointmentHistoryImpl(
    private val dataSource: AppointmentDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val transactionManager: TransactionManager
) : GetAppointmentHistory {
    override suspend fun invoke(
        userId: Uuid,
        businessId: Uuid,
        limit: Int,
        offset: Long
    ): Result<Pagination<Appointment>> = transactionManager.transaction {
        permissionsDataSource.getPermissions(userId, businessId).assert(ObjectPermission.READ)
        dataSource.getAllPaginated(businessId, limit, offset)
    }
}