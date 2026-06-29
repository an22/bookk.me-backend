package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.domain
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.data.DataSource
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class AppointmentSubscriptionDataSourceImpl : DataSource(), AppointmentSubscriptionDataSource {

    override suspend fun getBusinessSnapshot(id: Uuid): BusinessSnapshot? = dbQuery {
        AppointmentBusinessTable.selectAll()
            .where { AppointmentBusinessTable.id eq id.toJavaUuid() }
            .map { it.domain() }
            .singleOrNull()
    }

    override suspend fun attachBusiness(snapshot: BusinessSnapshot) {
        dbQuery {
            AppointmentBusinessTable.insert {
                it[id] = snapshot.id.toJavaUuid()
                it[name] = snapshot.name
                it[address] = snapshot.address
                it[enabled] = true
                it[timeZone] = snapshot.timeZone.id
            }
        }
    }

    override suspend fun updateBusiness(snapshot: BusinessSnapshot) {
        dbQuery {
            AppointmentBusinessTable.update(
                where = { AppointmentBusinessTable.id eq snapshot.id.toJavaUuid() },
            ) {
                it[name] = snapshot.name
                it[address] = snapshot.address
                it[enabled] = snapshot.isEnabled
                it[timeZone] = snapshot.timeZone.id
                it[updatedAt] = Clock.System.now()
            }
        }
    }

    override suspend fun updateBusinessInfo(id: Uuid, name: String, address: String, timeZone: TimeZone) {
        dbQuery {
            AppointmentBusinessTable.update(
                where = { AppointmentBusinessTable.id eq id.toJavaUuid() },
            ) {
                it[AppointmentBusinessTable.name] = name
                it[AppointmentBusinessTable.address] = address
                it[AppointmentBusinessTable.timeZone] = timeZone.id
                it[AppointmentBusinessTable.updatedAt] = Clock.System.now()
            }
        }
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