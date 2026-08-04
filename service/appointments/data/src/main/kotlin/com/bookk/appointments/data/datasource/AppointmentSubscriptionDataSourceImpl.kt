package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.entity.AppointmentBusinessEntity
import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class AppointmentSubscriptionDataSourceImpl : DataSource(), AppointmentSubscriptionDataSource {

    override suspend fun getBusinessSnapshot(id: Uuid): BusinessSnapshot? = dbQuery {
        AppointmentBusinessEntity.findById(id.toJavaUuid())?.domain()
    }

    override suspend fun attachBusiness(snapshot: BusinessSnapshot) {
        dbQuery { AppointmentBusinessEntity.new(snapshot) }
    }

    override suspend fun updateBusiness(snapshot: BusinessSnapshot, updatedAt: Instant) {
        dbQuery { AppointmentBusinessEntity.findByIdAndUpdate(snapshot, updatedAt) }
    }

    override suspend fun detachBusiness(businessId: Uuid) {
        dbQuery {
            AppointmentBusinessTable.deleteWhere {
                AppointmentBusinessTable.id eq businessId.toJavaUuid()
            }
        }
    }

    override suspend fun enableBusiness(businessId: Uuid) {
        dbQuery {
            AppointmentBusinessTable.update(
                where = { AppointmentBusinessTable.id eq businessId.toJavaUuid() }
            ) {
                it[enabled] = true
                it[updatedAt] = Clock.System.now()
            }
        }
    }

    override suspend fun isBusinessEnabled(businessId: Uuid): Boolean = dbQuery {
        AppointmentBusinessTable.select(AppointmentBusinessTable.enabled)
            .where { AppointmentBusinessTable.id eq businessId.toJavaUuid() }
            .map { it[AppointmentBusinessTable.enabled] }
            .singleOrNull() ?: false
    }

    override suspend fun disableBusiness(businessId: Uuid) {
        dbQuery {
            AppointmentBusinessTable.update(
                where = { AppointmentBusinessTable.id eq businessId.toJavaUuid() }
            ) {
                it[enabled] = false
                it[updatedAt] = Clock.System.now()
            }
        }
    }
}
