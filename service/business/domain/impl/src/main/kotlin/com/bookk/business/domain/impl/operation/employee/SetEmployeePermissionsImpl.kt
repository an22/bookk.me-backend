package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.operation.SetEmployeePermissions
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

internal class SetEmployeePermissionsImpl(
    private val employeeDataSource: EmployeeDataSource,
    private val businessPermissionDataSource: BusinessPermissionDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : SetEmployeePermissions {
    override suspend fun invoke(
        requestUserId: Uuid,
        businessId: Uuid,
        employeeId: Uuid,
        grants: Map<BusinessResource, ResourcePermission>
    ): Result<Employee> = transactionManager.transaction {
        businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.EMPLOYEES)
            .assert(PermissionAction.UPDATE)
        val employee = employeeDataSource.getEmployee(businessId, employeeId) ?: throw Error.NotFound()
        if (grants.isEmpty()) return@transaction employee
        val callersOwnPermissions = businessPermissionDataSource.getPermissions(requestUserId, businessId)
        if (grants.any { (resource, permission) -> !callersOwnPermissions[resource].covers(permission) }) {
            throw SetEmployeePermissions.Error.InsufficientGrant()
        }
        businessPermissionDataSource.setPermissions(employee, grants)
        val updated = employee.copy(permissions = employee.permissions.with(grants))
        eventProducer.send(
            BusinessEvent.EmployeePermissionsChanged(
                employeeUserId = employee.userId,
                businessId = businessId,
                permissions = updated.permissions
            )
        )
        updated
    }
}
