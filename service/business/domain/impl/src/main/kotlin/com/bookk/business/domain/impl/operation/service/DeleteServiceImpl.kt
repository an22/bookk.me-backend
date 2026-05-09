package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.operation.DeleteService
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class DeleteServiceImpl(
    private val dataSource: ServiceDataSource,
    private val transactionManager: TransactionManager
) : DeleteService {
    override suspend fun invoke(id: Uuid): Result<Unit> = transactionManager.transaction {
        dataSource.deleteService(id)
    }
}