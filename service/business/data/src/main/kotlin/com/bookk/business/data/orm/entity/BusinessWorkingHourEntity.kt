package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import library.schedule.WorkHour
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID

internal class BusinessWorkingHourEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var business by BusinessEntity referencedOn BusinessWorkingHoursTable.businessId
    var dayOfWeek by BusinessWorkingHoursTable.dayOfWeek
    var startTime by BusinessWorkingHoursTable.startTime
    var endTime by BusinessWorkingHoursTable.endTime

    fun domain(): WorkHour = WorkHour(from = startTime, to = endTime)

    companion object : DecoratorUUIDEntityClass<BusinessWorkingHourEntity>(BusinessWorkingHoursTable) {
        fun batchReplace(businessId: UUID, hours: Map<DayOfWeek, List<WorkHour>>) {
            BusinessWorkingHoursTable.deleteWhere { BusinessWorkingHoursTable.businessId eq businessId }
            val rows = hours.flatMap { (dayOfWeek, workingTime) -> workingTime.map { dayOfWeek to it } }
            BusinessWorkingHoursTable.batchInsert(rows) { (dayOfWeek, workHour) ->
                this[BusinessWorkingHoursTable.businessId] = businessId
                this[BusinessWorkingHoursTable.dayOfWeek] = dayOfWeek.isoDayNumber.toByte()
                this[BusinessWorkingHoursTable.startTime] = workHour.from
                this[BusinessWorkingHoursTable.endTime] = workHour.to
            }
        }
    }
}

internal fun Iterable<BusinessWorkingHourEntity>.toWorkingHours(): Map<DayOfWeek, List<WorkHour>> {
    return this
        .groupBy { DayOfWeek(it.dayOfWeek.toInt()) }
        .mapValues { (_, rows) -> rows.map { it.domain() } }
}
