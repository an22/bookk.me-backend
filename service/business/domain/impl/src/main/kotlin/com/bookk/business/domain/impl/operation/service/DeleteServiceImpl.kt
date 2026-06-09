package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.operation.DeleteService
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class DeleteServiceImpl(
    private val dataSource: ServiceDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : DeleteService {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, id: Uuid): Result<Unit> {
        return transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.EDIT)
            dataSource.deleteService(id)
        }
    }
}