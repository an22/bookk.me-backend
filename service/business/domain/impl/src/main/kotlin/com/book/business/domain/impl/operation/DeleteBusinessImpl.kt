package com.book.business.domain.impl.operation

import com.book.business.domain.api.operation.DeleteBusiness
import com.book.business.domain.datasource.BusinessDataSource
import kotlin.uuid.Uuid

internal class DeleteBusinessImpl(
    private val businessDataSource: BusinessDataSource
) : DeleteBusiness {
    override suspend fun invoke(userId: Uuid): Result<Unit> = runCatching {
        businessDataSource.deleteUserBusinesses(userId)
    }
}