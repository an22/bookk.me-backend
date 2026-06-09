package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.operation.GetDashboardBusiness
import com.bookk.business.domain.api.business.operation.GetDashboardBusiness.Error.NotFound
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class GetDashboardBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
): GetDashboardBusiness {
    override suspend fun invoke(userId: Uuid): Result<Business> = transactionManager.transaction {
        businessDataSource.getDashboardBusiness(userId) ?: throw NotFound()
    }
}