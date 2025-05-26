package com.book.business.domain.impl.operation

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.operation.GetDashboardBusiness
import com.book.business.domain.api.operation.GetDashboardBusiness.Error.NotFound
import com.book.business.domain.datasource.BusinessDataSource

internal class GetDashboardBusinessImpl(
    private val businessDataSource: BusinessDataSource
): GetDashboardBusiness {
    override suspend fun invoke(userId: Long): Result<Business> = runCatching{
        businessDataSource.getDashboardBusiness(userId) ?: throw NotFound
    }
}