package com.book.business.domain.impl.operation

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.operation.CreateBusiness
import com.book.business.domain.datasource.BusinessDataSource
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