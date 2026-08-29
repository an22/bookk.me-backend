package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeRole
import com.bookk.business.domain.api.employee.operation.PromoteEmployee
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.Error
import com.bookk.server.business.client.api.event.BusinessEvent
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class PromoteEmployeeImpl(
    private val employeeDataSource: EmployeeDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer
) : PromoteEmployee {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, employeeId: Uuid, role: EmployeeRole): Result<Unit> =
        transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.OWNER)
            val employee = employeeDataSource.getEmployee(businessId, employeeId) ?: throw Error.NotFound()
            val permission = role.toPermission().int
            businessDataSource.setUserPermissions(employee.userId, businessId, permission)
            eventProducer.send(
                BusinessEvent.EmployeePermissionChanged(
                    employeeUserId = employee.userId,
                    businessId = businessId,
                    permission = permission
                )
            )
        }
}
