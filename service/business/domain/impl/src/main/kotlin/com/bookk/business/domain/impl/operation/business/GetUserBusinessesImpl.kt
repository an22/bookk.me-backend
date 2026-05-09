package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.UserBusinesses
import com.bookk.business.domain.api.business.operation.GetUserBusinesses
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class GetUserBusinessesImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : GetUserBusinesses {
    override suspend fun invoke(userId: Uuid): Result<UserBusinesses> = transactionManager.transaction {
        businessDataSource.getUserBusinesses(userId = userId)
    }
}