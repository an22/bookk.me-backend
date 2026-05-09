package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.UpdateService
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class UpdateServiceImpl(
    private val dataSource: ServiceDataSource,
    private val transactionManager: TransactionManager
): UpdateService {
    override suspend fun invoke(service: Service): Result<Service> = transactionManager.transaction {
        dataSource.editService(service)
    }
}