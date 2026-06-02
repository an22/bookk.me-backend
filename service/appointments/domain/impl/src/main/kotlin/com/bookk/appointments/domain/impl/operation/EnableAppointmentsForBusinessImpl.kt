package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import kotlin.uuid.Uuid

internal class EnableAppointmentsForBusinessImpl(
    private val dataSource: AppointmentDataSource,
    private val transactionManager: TransactionManager
) : EnableAppointmentsForBusiness {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<Unit> = transactionManager.transaction {
        dataSource.attachBusiness(businessId)
        dataSource.initPermissions(userId, businessId, ObjectPermission.OWNER.int)
    }
}