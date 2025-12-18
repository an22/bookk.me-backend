package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.entity.Business
import com.bookk.business.domain.api.operation.GetDashboardBusiness
import com.bookk.business.domain.api.operation.GetDashboardBusiness.Error.NotFound
import com.bookk.business.domain.datasource.BusinessDataSource
import kotlin.uuid.Uuid

internal class GetDashboardBusinessImpl(
    private val businessDataSource: BusinessDataSource
): GetDashboardBusiness {
    override suspend fun invoke(userId: Uuid): Result<Business> = runCatching{
        businessDataSource.getDashboardBusiness(userId) ?: throw NotFound
    }
}