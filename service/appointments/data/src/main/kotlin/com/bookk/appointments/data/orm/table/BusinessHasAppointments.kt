package com.bookk.appointments.data.orm.table

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.core.data.database.BaseUUIDTable
import org.jetbrains.exposed.v1.core.ResultRow
import kotlin.uuid.toKotlinUuid

object BusinessHasAppointments: BaseUUIDTable("business_has_appointments") {
    val enabled = bool("enabled")
    val name = varchar("name", 512)
    val address = varchar("address", 512)
}

fun ResultRow.domain(): BusinessSnapshot {
    return BusinessSnapshot(
        id = this[BusinessHasAppointments.id].value.toKotlinUuid(),
        name = this[BusinessHasAppointments.name],
        address = this[BusinessHasAppointments.address],
        isEnabled = this[BusinessHasAppointments.enabled]
    )
}