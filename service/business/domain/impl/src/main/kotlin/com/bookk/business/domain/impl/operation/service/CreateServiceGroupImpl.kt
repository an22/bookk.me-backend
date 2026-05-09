package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.api.service.operation.CreateServiceGroup
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class CreateServiceGroupImpl(
    private val dataSource: ServiceDataSource,
    private val transactionManager: TransactionManager
) : CreateServiceGroup {
    override suspend fun invoke(service: ServiceGroup): Result<ServiceGroup> = transactionManager.transaction {
        dataSource.createServiceGroup(service)
    }
}