package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID

internal class DayOffEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var settings by SettingsEntity referencedOn DayOffsTable.settingsId
    var startDate by DayOffsTable.startDate
    var endDate by DayOffsTable.endDate

    companion object : DecoratorUUIDEntityClass<DayOffEntity>(DayOffsTable) {
        fun batchInsert(settingsId: UUID, ranges: List<DayOffRange>) {
            DayOffsTable.deleteWhere { DayOffsTable.settingsId eq settingsId }
            DayOffsTable.batchInsert(ranges) {
                this[DayOffsTable.settingsId] = settingsId
                this[DayOffsTable.startDate] = it.start
                this[DayOffsTable.endDate] = it.end
            }
        }
    }
}