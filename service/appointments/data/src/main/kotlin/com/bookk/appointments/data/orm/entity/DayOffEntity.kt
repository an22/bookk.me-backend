package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class DayOffEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var settings by SettingsEntity referencedOn DayOffsTable.settingsId
    var date by DayOffsTable.date

    companion object : DecoratorUUIDEntityClass<DayOffEntity>(DayOffsTable) {
        fun batchInsert(settingsId: Uuid, dates: List<LocalDate>) {
            val uuid = settingsId.toJavaUuid()
            DayOffsTable.deleteWhere { DayOffsTable.settingsId eq uuid }
            DayOffsTable.batchInsert(dates) {
                this[DayOffsTable.settingsId] = uuid
            }
        }
    }
}