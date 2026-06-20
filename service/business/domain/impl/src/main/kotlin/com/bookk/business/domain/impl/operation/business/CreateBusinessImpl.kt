package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessCreateRequest
import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import kotlin.uuid.Uuid

internal class CreateBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : CreateBusiness {
    override suspend fun invoke(
        userId: Uuid,
        request: BusinessCreateRequest
    ): Result<Business> = transactionManager.transaction {
        if (request.name.length !in 2..512) throw CreateBusiness.Error.BusinessValidationError()
        if (businessDataSource.isBusinessExist(userId)) throw CreateBusiness.Error.BusinessExist()
        businessDataSource.createBusiness(userId, request.name, request.currencyCode, request.timeZone).also {
            businessDataSource.setUserPermissions(userId, it.id, ObjectPermission.OWNER.int)
        }
    }
}