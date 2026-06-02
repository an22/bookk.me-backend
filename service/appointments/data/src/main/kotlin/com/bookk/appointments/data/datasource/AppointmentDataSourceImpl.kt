package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.data.orm.table.BusinessHasAppointments
import com.bookk.appointments.data.orm.table.UserHasAppointmentPermissions
import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class AppointmentDataSourceImpl : DataSource(), AppointmentDataSource {
    override suspend fun attachBusiness(businessId: Uuid) {
        dbQuery {
            BusinessHasAppointments.insertIgnore {
                it[id] = businessId.toJavaUuid()
            }
        }
    }

    override suspend fun initPermissions(
        userId: Uuid,
        businessId: Uuid,
        permissions: Int
    ) {
        dbQuery {
            UserHasAppointmentPermissions.insertIgnore {
                it[this.userId] = userId.toJavaUuid()
                it[this.businessId] = businessId.toJavaUuid()
                it[this.permission] = permissions
            }
        }
    }

    override suspend fun getPermissions(userId: Uuid, businessId: Uuid): Int? {
        return dbQuery {
            UserHasAppointmentPermissions.select(UserHasAppointmentPermissions.permission)
                .where { (UserHasAppointmentPermissions.userId eq userId.toJavaUuid()) and (UserHasAppointmentPermissions.businessId eq businessId.toJavaUuid()) }
                .singleOrNull()?.let { it[UserHasAppointmentPermissions.permission] }
        }
    }

    override suspend fun hasOverlapsWith(request: AppointmentRequest): Boolean {
        return dbQuery {
            AppointmentTable.select(AppointmentTable.id)
                .where {
                    (AppointmentTable.businessId eq request.businessId.toJavaUuid())
                        .and(AppointmentTable.userId eq request.userId.toJavaUuid())
                        .and(AppointmentTable.dateStart.less(request.date + request.service.duration))
                        .and(AppointmentTable.dateEnd.greater(request.date))
                }
                .empty()
                .not()
        }
    }

    override suspend fun create(request: AppointmentRequest): Appointment {
        TODO("Not yet implemented")
    }
}