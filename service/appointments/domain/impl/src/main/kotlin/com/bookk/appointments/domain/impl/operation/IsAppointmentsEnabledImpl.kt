package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.operation.IsAppointmentsEnabled
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class IsAppointmentsEnabledImpl(
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val permissionsDataSource: PermissionsDataSource,
    private val transactionManager: TransactionManager
) : IsAppointmentsEnabled {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<Boolean> = transactionManager.transaction{
        permissionsDataSource.getPermissions(userId, businessId).assert(ObjectPermission.READ)
        subscriptionDataSource.isBusinessEnabled(businessId)
    }
}