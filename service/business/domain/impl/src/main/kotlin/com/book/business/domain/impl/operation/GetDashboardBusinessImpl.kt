package com.book.business.domain.impl.operation

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.operation.GetDashboardBusiness
import com.book.business.domain.api.operation.GetDashboardBusiness.Error.NotFound
import com.book.business.domain.datasource.BusinessDataSource
import kotlin.uuid.Uuid

internal class GetDashboardBusinessImpl(
    private val businessDataSource: BusinessDataSource
): GetDashboardBusiness {
    override suspend fun invoke(userId: Uuid): Result<Business> = runCatching{
        businessDataSource.getDashboardBusiness(userId) ?: throw NotFound
    }
}