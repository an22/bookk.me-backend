package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.uuid.toJavaUuid

internal class AppointmentDataSourceImpl : DataSource(), AppointmentDataSource {

    override suspend fun hasOverlapsWith(request: AppointmentRequest): Boolean {
        return dbQuery {
            AppointmentTable.select(AppointmentTable.id)
                .where {
                    (AppointmentTable.businessId eq request.businessId.toJavaUuid())
                        .and(AppointmentTable.userId eq request.userId.toJavaUuid())
                        .and(AppointmentTable.dateStart.less(request.date + request.service.duration))
                        .and(AppointmentTable.dateEnd.greater(request.date))
                }
                .limit(1)
                .empty()
                .not()
        }
    }

    override suspend fun create(request: AppointmentRequest): Appointment {
        TODO("Not yet implemented")
    }
}