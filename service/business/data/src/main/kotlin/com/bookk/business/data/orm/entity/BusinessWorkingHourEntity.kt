package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.business.domain.api.business.entity.WorkHour
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.isoDayNumber
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

    companion object : DecoratorUUIDEntityClass<BusinessWorkingHourEntity>(BusinessWorkingHoursTable) {
        fun batchReplace(businessId: UUID, hours: List<WorkHour>) {
            BusinessWorkingHoursTable.deleteWhere { BusinessWorkingHoursTable.businessId eq businessId }
            BusinessWorkingHoursTable.batchInsert(hours) {
                this[BusinessWorkingHoursTable.businessId] = businessId
                this[BusinessWorkingHoursTable.dayOfWeek] = it.dayOfWeek.isoDayNumber.toByte()
                this[BusinessWorkingHoursTable.startTime] = it.from
                this[BusinessWorkingHoursTable.endTime] = it.to
            }
        }
    }
}
