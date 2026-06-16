package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.BusinessHasAppointments
import com.bookk.appointments.data.orm.table.domain
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class AppointmentSubscriptionDataSourceImpl : DataSource(), AppointmentSubscriptionDataSource {

    override suspend fun getBusinessSnapshot(id: Uuid): BusinessSnapshot? = dbQuery {
        BusinessHasAppointments.selectAll()
            .where { BusinessHasAppointments.id eq id.toJavaUuid() }
            .map { it.domain() }
            .singleOrNull()
    }

    override suspend fun attachBusiness(snapshot: BusinessSnapshot) {
        dbQuery {
            BusinessHasAppointments.insert {
                it[id] = snapshot.id.toJavaUuid()
                it[name] = snapshot.name
                it[address] = snapshot.address
                it[enabled] = true
            }
        }
    }

    override suspend fun updateBusiness(snapshot: BusinessSnapshot) {
        dbQuery {
            BusinessHasAppointments.update(
                where = { BusinessHasAppointments.id eq snapshot.id.toJavaUuid() },
            ) {
                it[name] = snapshot.name
                it[address] = snapshot.address
                it[enabled] = snapshot.isEnabled
            }
        }
    }

    override suspend fun updateBusinessInfo(id: Uuid, name: String, address: String) {
        dbQuery {
            BusinessHasAppointments.update(
                where = { BusinessHasAppointments.id eq id.toJavaUuid() },
            ) {
                it[BusinessHasAppointments.name] = name
                it[BusinessHasAppointments.address] = address
            }
        }
    }

    override suspend fun detachBusiness(businessId: Uuid) {
        dbQuery {
            BusinessHasAppointments.deleteWhere {
                BusinessHasAppointments.id eq businessId.toJavaUuid()
            }
        }
    }

    override suspend fun enableBusiness(businessId: Uuid) {
        dbQuery {
            BusinessHasAppointments.update(
                where = { BusinessHasAppointments.id eq businessId.toJavaUuid() }
            ) {
                it[enabled] = true
            }
        }
    }

    override suspend fun isBusinessEnabled(businessId: Uuid): Boolean = dbQuery {
        BusinessHasAppointments.select(BusinessHasAppointments.enabled)
            .where { BusinessHasAppointments.id eq businessId.toJavaUuid() }
            .map { it[BusinessHasAppointments.enabled] }
            .singleOrNull() ?: false
    }

    override suspend fun disableBusiness(businessId: Uuid) {
        dbQuery {
            BusinessHasAppointments.update(
                where = { BusinessHasAppointments.id eq businessId.toJavaUuid() }
            ) {
                it[enabled] = false
            }
        }
    }
}