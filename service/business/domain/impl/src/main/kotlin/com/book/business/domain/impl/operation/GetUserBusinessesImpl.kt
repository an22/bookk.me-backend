package com.book.business.domain.impl.operation

import com.book.business.domain.api.entity.UserBusinesses
import com.book.business.domain.api.operation.GetUserBusinesses
import com.book.business.domain.datasource.BusinessDataSource
import kotlin.uuid.Uuid

internal class GetUserBusinessesImpl(
    private val businessDataSource: BusinessDataSource
) : GetUserBusinesses {
    override suspend fun invoke(userId: Uuid): Result<UserBusinesses> = runCatching {
        businessDataSource.getUserBusinesses(userId = userId)
    }
}