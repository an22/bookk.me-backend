package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.operation.GetEmployeePermissions
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import library.permissions.PermissionAction
import library.permissions.assert
import kotlin.uuid.Uuid

internal class GetEmployeePermissionsImpl(
    private val employeeDataSource: EmployeeDataSource,
    private val businessPermissionDataSource: BusinessPermissionDataSource,
    private val transactionManager: TransactionManager
) : GetEmployeePermissions {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, employeeId: Uuid): Result<BusinessPermissions> =
        transactionManager.transaction {
            businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.EMPLOYEES)
                .assert(PermissionAction.VIEW)
            val employee = employeeDataSource.getEmployee(businessId, employeeId) ?: throw Error.NotFound()
            businessPermissionDataSource.getPermissions(employee.userId, businessId)
        }
}
