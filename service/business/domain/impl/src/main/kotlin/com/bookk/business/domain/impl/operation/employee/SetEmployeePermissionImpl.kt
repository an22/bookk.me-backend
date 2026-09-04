package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.operation.SetEmployeePermission
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import com.bookk.server.business.client.api.event.BusinessEvent
import library.permissions.PermissionAction
import library.permissions.ResourcePermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class SetEmployeePermissionImpl(
    private val employeeDataSource: EmployeeDataSource,
    private val businessPermissionDataSource: BusinessPermissionDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : SetEmployeePermission {
    override suspend fun invoke(
        requestUserId: Uuid,
        businessId: Uuid,
        employeeId: Uuid,
        resource: BusinessResource,
        permission: ResourcePermission
    ): Result<BusinessPermissions> = transactionManager.transaction {
        businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.EMPLOYEES)
            .assert(PermissionAction.UPDATE)
        val employee = employeeDataSource.getEmployee(businessId, employeeId) ?: throw Error.NotFound()
        val callersOwnGrant = businessPermissionDataSource.getPermission(requestUserId, businessId, resource)
        if (!callersOwnGrant.covers(permission)) throw SetEmployeePermission.Error.InsufficientGrant()
        businessPermissionDataSource.setPermission(employee.userId, businessId, resource, permission)
        val updated = businessPermissionDataSource.getPermissions(employee.userId, businessId)
        eventProducer.send(
            BusinessEvent.EmployeePermissionsChanged(
                employeeUserId = employee.userId,
                businessId = businessId,
                permissions = updated
            )
        )
        updated
    }
}
