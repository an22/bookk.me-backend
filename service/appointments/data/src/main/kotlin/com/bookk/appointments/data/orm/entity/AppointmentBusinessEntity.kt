package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class AppointmentBusinessEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var enabled by AppointmentBusinessTable.enabled
    var name by AppointmentBusinessTable.name
    var address by AppointmentBusinessTable.address
    var timezone by AppointmentBusinessTable.timeZone

    companion object : DecoratorUUIDEntityClass<AppointmentBusinessEntity>(AppointmentBusinessTable)
}