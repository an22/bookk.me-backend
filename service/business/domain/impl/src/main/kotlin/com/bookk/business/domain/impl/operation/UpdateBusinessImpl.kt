package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.entity.BusinessUpdateModel
import com.bookk.business.domain.api.operation.UpdateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource

internal class UpdateBusinessImpl(
    private val businessDataSource: BusinessDataSource
) : UpdateBusiness {
    override suspend fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit> = runCatching {
        businessDataSource.updateBusiness(businessUpdateModel)
    }
}