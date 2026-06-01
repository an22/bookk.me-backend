package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.operation.DeleteServiceGroup
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class DeleteServiceGroupImpl(
    private val dataSource: ServiceDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : DeleteServiceGroup {

    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, id: Uuid): Result<Unit> {
        return transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.WRITE)
            dataSource.deleteServiceGroup(id)
        }
    }
}