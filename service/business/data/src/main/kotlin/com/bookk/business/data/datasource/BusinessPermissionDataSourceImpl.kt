package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessPermissionGrantsTable
import com.bookk.business.data.orm.table.EmployeeTable
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.core.data.DataSource
import library.permissions.EmployeeAccessSuspended
import library.permissions.ResourcePermission
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid

internal class BusinessPermissionDataSourceImpl : DataSource(), BusinessPermissionDataSource {

    override suspend fun getPermissions(userId: Uuid, businessId: Uuid): BusinessPermissions = dbQuery {
        BusinessPermissionGrantsTable.permissionsOf(
            BusinessPermissionGrantsTable
                .innerJoin(EmployeeTable)
                .selectAll()
                .where {
                    (BusinessPermissionGrantsTable.userId eq userId) and
                        (BusinessPermissionGrantsTable.businessId eq businessId) and
                        EmployeeTable.suspendedAt.isNull()
                }
        )
    }

    override suspend fun getPermission(userId: Uuid, businessId: Uuid, resource: BusinessResource): ResourcePermission {
        val grant = dbQuery {
            BusinessPermissionGrantsTable
                .innerJoin(EmployeeTable)
                .select(
                    BusinessPermissionGrantsTable.canView,
                    BusinessPermissionGrantsTable.canUpdate,
                    BusinessPermissionGrantsTable.canDelete,
                    EmployeeTable.suspendedAt
                )
                .where {
                    (BusinessPermissionGrantsTable.userId eq userId) and
                        (BusinessPermissionGrantsTable.businessId eq businessId) and
                        (BusinessPermissionGrantsTable.resource eq resource)
                }
                .singleOrNull()
        } ?: return ResourcePermission.NONE
        if (grant[EmployeeTable.suspendedAt] != null) throw EmployeeAccessSuspended()
        return ResourcePermission(
            view = grant[BusinessPermissionGrantsTable.canView],
            update = grant[BusinessPermissionGrantsTable.canUpdate],
            delete = grant[BusinessPermissionGrantsTable.canDelete]
        )
    }

    override suspend fun setPermissions(employee: Employee, grants: Map<BusinessResource, ResourcePermission>) = dbQuery {
        BusinessPermissionGrantsTable.upsertGrants(employee.id, employee.userId, employee.businessId, grants)
    }

    override suspend fun deleteUserPermissions(userId: Uuid) = dbQuery<Unit> {
        BusinessPermissionGrantsTable.deleteWhere { BusinessPermissionGrantsTable.userId eq userId }
    }
}
