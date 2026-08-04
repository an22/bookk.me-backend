package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.WorkingHoursTable
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

internal class WorkingHourEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var business by AppointmentBusinessEntity referencedOn WorkingHoursTable.businessId
    var dayOfWeek by WorkingHoursTable.dayOfWeek
    var startTime by WorkingHoursTable.startTime
    var endTime by WorkingHoursTable.endTime

    fun domain(): WorkHour = WorkHour(from = startTime, to = endTime)

    companion object : DecoratorUUIDEntityClass<WorkingHourEntity>(WorkingHoursTable) {
        fun batchReplace(businessId: UUID, hours: Map<DayOfWeek, List<WorkHour>>) {
            WorkingHoursTable.deleteWhere { WorkingHoursTable.businessId eq businessId }
            val rows = hours.flatMap { (dayOfWeek, workingTime) -> workingTime.map { dayOfWeek to it } }
            WorkingHoursTable.batchInsert(rows) { (dayOfWeek, workHour) ->
                this[WorkingHoursTable.businessId] = businessId
                this[WorkingHoursTable.dayOfWeek] = dayOfWeek.isoDayNumber.toByte()
                this[WorkingHoursTable.startTime] = workHour.from
                this[WorkingHoursTable.endTime] = workHour.to
            }
        }
    }
}

internal fun Iterable<WorkingHourEntity>.toWorkingHours(): Map<DayOfWeek, List<WorkHour>> {
    return this
        .groupBy { DayOfWeek(it.dayOfWeek.toInt()) }
        .mapValues { (_, rows) -> rows.map { it.domain() } }
}