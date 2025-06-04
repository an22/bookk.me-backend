package com.book.business.domain.impl.operation

import com.book.business.domain.api.entity.BusinessUpdateModel
import com.book.business.domain.api.operation.UpdateBusiness
import com.book.business.domain.datasource.BusinessDataSource

internal class UpdateBusinessImpl(
    private val businessDataSource: BusinessDataSource
) : UpdateBusiness {
    override suspend fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit> = runCatching {
        businessDataSource.updateBusiness(businessUpdateModel)
    }
}