package com.bookk.appointments.data.orm.table

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.core.data.database.BaseUUIDTable
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.core.ResultRow
import kotlin.uuid.toKotlinUuid

object AppointmentBusinessTable: BaseUUIDTable("business_has_appointments") {
    val enabled = bool("enabled")
    val name = varchar("name", 512)
    val address = varchar("address", 512)
    val timeZone = varchar("time_zone", 256)
}

fun ResultRow.domain(): BusinessSnapshot {
    return BusinessSnapshot(
        id = this[AppointmentBusinessTable.id].value.toKotlinUuid(),
        name = this[AppointmentBusinessTable.name],
        address = this[AppointmentBusinessTable.address],
        isEnabled = this[AppointmentBusinessTable.enabled],
        timeZone = TimeZone.of(this[AppointmentBusinessTable.timeZone]),
    )
}