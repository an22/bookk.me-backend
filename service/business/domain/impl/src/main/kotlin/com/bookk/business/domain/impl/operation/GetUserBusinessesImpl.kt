package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.entity.UserBusinesses
import com.bookk.business.domain.api.operation.GetUserBusinesses
import com.bookk.business.domain.datasource.BusinessDataSource
import kotlin.uuid.Uuid

internal class GetUserBusinessesImpl(
    private val businessDataSource: BusinessDataSource
) : GetUserBusinesses {
    override suspend fun invoke(userId: Uuid): Result<UserBusinesses> = runCatching {
        businessDataSource.getUserBusinesses(userId = userId)
    }
}