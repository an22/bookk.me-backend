package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.core.data.DecoratorUUIDEntityClass
import library.schedule.DayOffRange
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID

internal class BusinessDayOffEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var business by BusinessEntity referencedOn BusinessDayOffTable.businessId
    var startDate by BusinessDayOffTable.startDate
    var endDate by BusinessDayOffTable.endDate

    fun domain(): DayOffRange = DayOffRange(start = startDate, end = endDate)

    companion object : DecoratorUUIDEntityClass<BusinessDayOffEntity>(BusinessDayOffTable) {
        fun batchReplace(businessId: UUID, ranges: List<DayOffRange>) {
            BusinessDayOffTable.deleteWhere { BusinessDayOffTable.businessId eq businessId }
            BusinessDayOffTable.batchInsert(ranges) {
                this[BusinessDayOffTable.businessId] = businessId
                this[BusinessDayOffTable.startDate] = it.start
                this[BusinessDayOffTable.endDate] = it.end
            }
        }
    }
}
