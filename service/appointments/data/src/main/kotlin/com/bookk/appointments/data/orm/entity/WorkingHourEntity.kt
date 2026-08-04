package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.isoDayNumber
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

    companion object : DecoratorUUIDEntityClass<WorkingHourEntity>(WorkingHoursTable) {
        fun batchReplace(businessId: UUID, hours: List<WorkHour>) {
            WorkingHoursTable.deleteWhere { WorkingHoursTable.businessId eq businessId }
            WorkingHoursTable.batchInsert(hours) {
                this[WorkingHoursTable.businessId] = businessId
                this[WorkingHoursTable.dayOfWeek] = it.dayOfWeek.isoDayNumber.toByte()
                this[WorkingHoursTable.startTime] = it.from
                this[WorkingHoursTable.endTime] = it.to
            }
        }
    }
}