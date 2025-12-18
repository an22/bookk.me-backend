package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.entity.Business
import com.bookk.business.domain.api.operation.CreateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import kotlin.uuid.Uuid

internal class CreateBusinessImpl(
    private val businessDataSource: BusinessDataSource
) : CreateBusiness {
    override suspend fun invoke(userId: Uuid, name: String, currencyCode: String): Result<Business> = runCatching {
        if (name.length !in 2..512) throw CreateBusiness.Error.BusinessValidationError
        if (businessDataSource.isBusinessExist(userId)) throw CreateBusiness.Error.BusinessExist
        businessDataSource.createBusiness(userId, name, currencyCode)
    }
}