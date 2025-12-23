package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.entity.BusinessUpdateModel
import com.bookk.business.domain.api.operation.UpdateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class UpdateBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : UpdateBusiness {
    override suspend fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit> = transactionManager.transaction {
        businessDataSource.updateBusiness(businessUpdateModel)
    }
}