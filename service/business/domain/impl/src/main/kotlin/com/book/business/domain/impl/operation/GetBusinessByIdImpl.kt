package com.book.business.domain.impl.operation

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.operation.GetBusinessById
import com.book.business.domain.datasource.BusinessDataSource

internal class GetBusinessByIdImpl(
    private val businessDataSource: BusinessDataSource
) : GetBusinessById {
    override suspend fun invoke(id: Long): Result<Business> = runCatching {
        businessDataSource.getBusinessById(id) ?: throw GetBusinessById.Error.NotFound
    }
}