package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.UserHasAppointmentPermissions
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.uuid.Uuid

internal class PermissionsDataSourceImpl : DataSource(), PermissionsDataSource {

    override suspend fun initPermissions(
        userId: Uuid,
        businessId: Uuid,
        permissions: Int
    ) {
        dbQuery {
            UserHasAppointmentPermissions.insertIgnore {
                it[this.userId] = userId
                it[this.businessId] = businessId
                it[this.permission] = permissions
            }
        }
    }

    override suspend fun getPermissions(userId: Uuid, businessId: Uuid): Int? {
        return dbQuery {
            UserHasAppointmentPermissions.select(UserHasAppointmentPermissions.permission)
                .where { (UserHasAppointmentPermissions.userId eq userId) and (UserHasAppointmentPermissions.businessId eq businessId) }
                .singleOrNull()?.let { it[UserHasAppointmentPermissions.permission] }
        }
    }

}