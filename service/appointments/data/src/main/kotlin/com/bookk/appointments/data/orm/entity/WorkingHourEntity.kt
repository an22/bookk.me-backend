package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class WorkingHourEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var settings by SettingsEntity referencedOn WorkingHoursTable.settingsId
    var dayOfWeek by WorkingHoursTable.dayOfWeek
    var startTime by WorkingHoursTable.startTime
    var endTime by WorkingHoursTable.endTime

    companion object : DecoratorUUIDEntityClass<WorkingHourEntity>(WorkingHoursTable) {
        fun batchInsert(settingsId: Uuid, hours: List<WorkHour>) {
            val uuid = settingsId.toJavaUuid()
            WorkingHoursTable.deleteWhere { WorkingHoursTable.settingsId eq uuid }
            WorkingHoursTable.batchInsert(hours) {
                this[WorkingHoursTable.settingsId] = uuid
                this[WorkingHoursTable.dayOfWeek] = it.dayOfWeek.ordinal.toByte()
                this[WorkingHoursTable.startTime] = it.from
                this[WorkingHoursTable.endTime] = it.to
            }
        }
    }
}