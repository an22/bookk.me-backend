package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.UserHasAppointmentPermissions
import com.bookk.appointments.domain.datasource.AppointmentPermissionDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.Uuid

internal class AppointmentPermissionDataSourceImpl : DataSource(), AppointmentPermissionDataSource {

    override suspend fun setPermissions(
        userId: Uuid,
        businessId: Uuid,
        permissions: Int
    ) {
        dbQuery {
            UserHasAppointmentPermissions.upsert {
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

    override suspend fun deleteForUser(userId: Uuid) = dbQuery<Unit> {
        UserHasAppointmentPermissions.deleteWhere { UserHasAppointmentPermissions.userId eq userId }
    }
}