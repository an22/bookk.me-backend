package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.operation.GetBusinessPermission
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class GetBusinessPermissionImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : GetBusinessPermission {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<Int> = transactionManager.transaction {
        businessDataSource.getPermission(userId, businessId) ?: GetBusinessPermission.NO_PERMISSION
    }
}
