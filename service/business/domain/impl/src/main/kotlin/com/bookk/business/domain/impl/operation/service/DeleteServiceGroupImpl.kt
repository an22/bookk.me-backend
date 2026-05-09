package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.operation.DeleteServiceGroup
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class DeleteServiceGroupImpl(
    private val dataSource: ServiceDataSource,
    private val transactionManager: TransactionManager
): DeleteServiceGroup {

    override suspend fun invoke(id: Uuid): Result<Unit> = transactionManager.transaction {
        dataSource.deleteServiceGroup(id)
    }
}