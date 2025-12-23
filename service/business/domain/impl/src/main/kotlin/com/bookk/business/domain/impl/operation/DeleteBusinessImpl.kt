package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.operation.DeleteBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class DeleteBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : DeleteBusiness {
    override suspend fun invoke(userId: Uuid): Result<Unit> = transactionManager.transaction {
        businessDataSource.deleteUserBusinesses(userId)
    }
}