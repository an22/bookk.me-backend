package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.Uuid

internal class BusinessPermissionDataSourceImpl : DataSource(), BusinessPermissionDataSource {

    override suspend fun getPermission(userId: Uuid, businessId: Uuid): Int? = dbQuery {
        BusinessPermissionsTable.select(
            BusinessPermissionsTable.permission
        )
            .where { (BusinessPermissionsTable.userId eq userId) and (BusinessPermissionsTable.businessId eq businessId) }
            .singleOrNull()
            ?.get(BusinessPermissionsTable.permission)
    }

    override suspend fun setUserPermissions(userId: Uuid, businessId: Uuid, permission: Int) {
        BusinessPermissionsTable.upsert {
            it[this.userId] = userId
            it[this.businessId] = businessId
            it[this.permission] = permission
        }
    }

    override suspend fun deleteUserPermissions(userId: Uuid) = dbQuery<Unit> {
        BusinessPermissionsTable.deleteWhere { BusinessPermissionsTable.userId eq userId }
    }
}
