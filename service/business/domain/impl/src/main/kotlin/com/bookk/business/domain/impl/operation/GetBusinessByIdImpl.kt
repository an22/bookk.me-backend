package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.entity.Business
import com.bookk.business.domain.api.operation.GetBusinessById
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class GetBusinessByIdImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : GetBusinessById {
    override suspend fun invoke(id: Uuid): Result<Business> = transactionManager.transaction {
        businessDataSource.getBusinessById(id) ?: throw GetBusinessById.Error.NotFound
    }
}