package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessCreateRequest
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ResourcePermission
import library.validation.NameValidator
import kotlin.uuid.Uuid

internal class CreateBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val businessPermissionDataSource: BusinessPermissionDataSource,
    private val transactionManager: TransactionManager
) : CreateBusiness {
    override suspend fun invoke(
        userId: Uuid,
        request: BusinessCreateRequest
    ): Result<Business> = transactionManager.transaction {
        if (!NameValidator.isValid(request.name)) {
            throw CreateBusiness.Error.BusinessValidationError()
        }
        if (businessDataSource.isBusinessExist(userId)) throw CreateBusiness.Error.BusinessExist()
        val business = businessDataSource.createBusiness(userId, request.name, request.currencyCode, request.timeZone)
        BusinessResource.entries.forEach { resource ->
            businessPermissionDataSource.setPermission(userId, business.id, resource, ResourcePermission.FULL)
        }
        business.copy(permissions = BusinessPermissions.FULL)
    }
}
