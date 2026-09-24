package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessPermissionGrantsTable
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.core.data.DataSource
import library.permissions.ResourcePermission
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid

internal class BusinessPermissionDataSourceImpl : DataSource(), BusinessPermissionDataSource {

    override suspend fun getPermissions(userId: Uuid, businessId: Uuid): BusinessPermissions = dbQuery {
        BusinessPermissionGrantsTable.permissionsOf(
            BusinessPermissionGrantsTable
                .selectAll()
                .where { (BusinessPermissionGrantsTable.userId eq userId) and (BusinessPermissionGrantsTable.businessId eq businessId) }
        )
    }

    override suspend fun getPermission(userId: Uuid, businessId: Uuid, resource: BusinessResource): ResourcePermission = dbQuery {
        BusinessPermissionGrantsTable
            .select(BusinessPermissionGrantsTable.canView, BusinessPermissionGrantsTable.canUpdate, BusinessPermissionGrantsTable.canDelete)
            .where {
                (BusinessPermissionGrantsTable.userId eq userId) and
                    (BusinessPermissionGrantsTable.businessId eq businessId) and
                    (BusinessPermissionGrantsTable.resource eq resource)
            }
            .singleOrNull()
            ?.let {
                ResourcePermission(
                    view = it[BusinessPermissionGrantsTable.canView],
                    update = it[BusinessPermissionGrantsTable.canUpdate],
                    delete = it[BusinessPermissionGrantsTable.canDelete]
                )
            } ?: ResourcePermission.NONE
    }

    override suspend fun setPermissions(employee: Employee, grants: Map<BusinessResource, ResourcePermission>) = dbQuery {
        BusinessPermissionGrantsTable.upsertGrants(employee.id, employee.userId, employee.businessId, grants)
    }

    override suspend fun deleteUserPermissions(userId: Uuid) = dbQuery<Unit> {
        BusinessPermissionGrantsTable.deleteWhere { BusinessPermissionGrantsTable.userId eq userId }
    }
}
