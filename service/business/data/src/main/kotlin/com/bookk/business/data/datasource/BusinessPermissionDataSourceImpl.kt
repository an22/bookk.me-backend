package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessPermissionGrantsTable
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.core.data.DataSource
import library.permissions.ResourcePermission
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.Uuid

internal class BusinessPermissionDataSourceImpl : DataSource(), BusinessPermissionDataSource {

    override suspend fun getPermissions(userId: Uuid, businessId: Uuid): BusinessPermissions = dbQuery {
        val grants = BusinessPermissionGrantsTable
            .select(BusinessPermissionGrantsTable.resource, BusinessPermissionGrantsTable.canView, BusinessPermissionGrantsTable.canUpdate, BusinessPermissionGrantsTable.canDelete)
            .where { (BusinessPermissionGrantsTable.userId eq userId) and (BusinessPermissionGrantsTable.businessId eq businessId) }
            .associate { row ->
                row[BusinessPermissionGrantsTable.resource] to ResourcePermission(
                    view = row[BusinessPermissionGrantsTable.canView],
                    update = row[BusinessPermissionGrantsTable.canUpdate],
                    delete = row[BusinessPermissionGrantsTable.canDelete]
                )
            }
        BusinessPermissions(
            business = grants[BusinessResource.BUSINESS] ?: ResourcePermission.NONE,
            employees = grants[BusinessResource.EMPLOYEES] ?: ResourcePermission.NONE,
            clients = grants[BusinessResource.CLIENTS] ?: ResourcePermission.NONE,
            services = grants[BusinessResource.SERVICES] ?: ResourcePermission.NONE,
            appointments = grants[BusinessResource.APPOINTMENTS] ?: ResourcePermission.NONE
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

    override suspend fun setPermission(userId: Uuid, businessId: Uuid, resource: BusinessResource, permission: ResourcePermission) {
        dbQuery {
            BusinessPermissionGrantsTable.upsert {
                it[this.userId] = userId
                it[this.businessId] = businessId
                it[this.resource] = resource
                it[this.canView] = permission.view
                it[this.canUpdate] = permission.update
                it[this.canDelete] = permission.delete
            }
        }
    }

    override suspend fun deleteUserPermissions(userId: Uuid) = dbQuery<Unit> {
        BusinessPermissionGrantsTable.deleteWhere { BusinessPermissionGrantsTable.userId eq userId }
    }
}
