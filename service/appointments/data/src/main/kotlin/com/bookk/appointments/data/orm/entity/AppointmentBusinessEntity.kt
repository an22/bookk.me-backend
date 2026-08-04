package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.map.toWorkingDays
import com.bookk.appointments.data.map.toWorkingDaysMask
import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.appointments.domain.api.entity.WorkingSchedule
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class AppointmentBusinessEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var enabled by AppointmentBusinessTable.enabled
    var name by AppointmentBusinessTable.name
    var address by AppointmentBusinessTable.address
    var timezone by AppointmentBusinessTable.timeZone
    var workingDays by AppointmentBusinessTable.workingDays
    var sourceUpdatedAt by AppointmentBusinessTable.sourceUpdatedAt
    var updatedAt by AppointmentBusinessTable.updatedAt
    val workingHours by WorkingHourEntity referrersOn WorkingHoursTable.businessId
    val dayOffs by DayOffEntity referrersOn DayOffsTable.businessId

    fun domain(): BusinessSnapshot = BusinessSnapshot(
        id = id.value.toKotlinUuid(),
        name = name,
        address = address,
        timeZone = TimeZone.of(timezone),
        isEnabled = enabled,
        schedule = WorkingSchedule(
            workingDays = workingDays.toWorkingDays(),
            workingHours = workingHours
                .map {
                    WorkHour(
                        dayOfWeek = DayOfWeek(it.dayOfWeek.toInt()),
                        from = it.startTime,
                        to = it.endTime
                    )
                }
                .groupBy { it.dayOfWeek }
        ),
        dayOffs = dayOffs.map { DayOffRange(it.startDate, it.endDate) }
    )

    private fun replaceSchedule(snapshot: BusinessSnapshot) {
        WorkingHourEntity.batchReplace(id.value, snapshot.schedule.list().flatMap { it.workingTime })
        DayOffEntity.batchReplace(id.value, snapshot.dayOffs)
    }

    companion object : DecoratorUUIDEntityClass<AppointmentBusinessEntity>(AppointmentBusinessTable) {

        fun new(snapshot: BusinessSnapshot): AppointmentBusinessEntity = new(snapshot.id.toJavaUuid()) {
            name = snapshot.name
            address = snapshot.address
            timezone = snapshot.timeZone.id
            enabled = true
            workingDays = snapshot.schedule.toWorkingDaysMask()
        }.apply { replaceSchedule(snapshot) }

        fun findByIdAndUpdate(snapshot: BusinessSnapshot, updatedAt: Instant) =
            findByIdAndUpdate(snapshot.id.toJavaUuid()) {
                val sourceUpdatedAt = it.sourceUpdatedAt
                if (sourceUpdatedAt == null || sourceUpdatedAt < updatedAt) {
                    it.name = snapshot.name
                    it.address = snapshot.address
                    it.timezone = snapshot.timeZone.id
                    it.workingDays = snapshot.schedule.toWorkingDaysMask()
                    it.sourceUpdatedAt = updatedAt
                    it.updatedAt = Clock.System.now()
                    it.replaceSchedule(snapshot)
                }
            }
    }
}
