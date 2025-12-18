package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.operation.DeleteBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import kotlin.uuid.Uuid

internal class DeleteBusinessImpl(
    private val businessDataSource: BusinessDataSource
) : DeleteBusiness {
    override suspend fun invoke(userId: Uuid): Result<Unit> = runCatching {
        businessDataSource.deleteUserBusinesses(userId)
    }
}