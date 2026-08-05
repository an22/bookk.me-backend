package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.operation.GetBusinessPermission
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import kotlin.uuid.Uuid

internal class GetBusinessPermissionImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : GetBusinessPermission {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<ObjectPermission> =
        transactionManager.transaction {
            ObjectPermission.of(businessDataSource.getPermission(userId, businessId))
        }
}
