package com.book.business.domain.impl.operation

import com.book.business.domain.api.entity.UserBusinesses
import com.book.business.domain.api.operation.GetUserBusinesses
import com.book.business.domain.datasource.BusinessDataSource

internal class GetUserBusinessesImpl(
    private val businessDataSource: BusinessDataSource
) : GetUserBusinesses {
    override suspend fun invoke(userId: Long): Result<UserBusinesses> = runCatching {
        businessDataSource.getUserBusinesses(userId = userId)
    }
}