package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.BusinessHasAppointments
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class AppointmentSubscriptionDataSourceImpl : DataSource(), AppointmentSubscriptionDataSource {
    override suspend fun attachBusiness(businessId: Uuid) {
        dbQuery {
            BusinessHasAppointments.upsert {
                it[id] = businessId.toJavaUuid()
                it[enabled] = true
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