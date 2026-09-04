package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class SyncEmployeePermission(
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val appointmentPermissionDataSource: AppointmentPermissionDataSource,
    private val transactionManager: TransactionManager
) {

    suspend operator fun invoke(
        userId: Uuid,
        businessId: Uuid,
        permission: Int
    ) = transactionManager.transaction {
        if (subscriptionDataSource.isBusinessEnabled(businessId)) {
            appointmentPermissionDataSource.setPermissions(userId, businessId, permission)
        }
    }
}
