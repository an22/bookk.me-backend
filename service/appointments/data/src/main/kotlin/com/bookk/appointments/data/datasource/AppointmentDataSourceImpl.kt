package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.entity.AppointmentEntity
import com.bookk.appointments.data.orm.table.AppointmentRequestTable
import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.core.data.DataSource
import com.bookk.core.domain.entity.Error
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class AppointmentDataSourceImpl : DataSource(), AppointmentDataSource {

    override suspend fun get(id: Uuid): Appointment = dbQuery {
        AppointmentEntity.findById(id.toJavaUuid())
            ?.domain() ?: throw Error.NotFound()
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
                .limit(1)
                .empty()
                .not()
        }
    }

    override suspend fun hasOverlapsWith(appointment: Appointment): Boolean {
        return dbQuery {
            AppointmentTable.select(AppointmentTable.id)
                .where {
                    (AppointmentTable.businessId eq appointment.businessId.toJavaUuid())
                        .and(AppointmentTable.userId eq appointment.userId.toJavaUuid())
                        .and(AppointmentTable.dateStart.less(appointment.date + appointment.service.duration))
                        .and(AppointmentTable.dateEnd.greater(appointment.date))
                        .and(AppointmentTable.id neq appointment.id.toJavaUuid())
                }
                .limit(1)
                .empty()
                .not()
        }
    }

    override suspend fun create(request: AppointmentRequest): Appointment = dbQuery {
        AppointmentEntity.new(request).domain()
    }

    override suspend fun update(appointment: Appointment): Appointment = dbQuery {
        AppointmentEntity.findByIdAndUpdate(appointment)
            ?.domain() ?: throw Error.NotFound()
    }

    override suspend fun delete(id: Uuid) = dbQuery<Unit> {
        AppointmentTable.deleteWhere {
            AppointmentTable.id eq id.toJavaUuid()
        }
    }

    override suspend fun cancel(id: Uuid, reason: String): Appointment = dbQuery {
        AppointmentEntity.findByIdAndUpdate(id.toJavaUuid()) {
            it.status = AppointmentStatus.CANCELLED
            it.cancellationReason = reason
        }?.domain() ?: throw Error.NotFound()
    }

    override suspend fun getAll(businessId: Uuid): List<Appointment> = dbQuery {
        AppointmentEntity
            .find { AppointmentRequestTable.businessId eq businessId.toJavaUuid() }
            .map { it.domain() }
    }
}