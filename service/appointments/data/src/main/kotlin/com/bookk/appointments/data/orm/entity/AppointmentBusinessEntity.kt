package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.TimeZone
import library.schedule.Schedule
import library.schedule.toWorkingDays
import library.schedule.toWorkingDaysMask
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

    fun schedule(): Schedule = Schedule(
        workingDays = workingDays.toWorkingDays(),
        workingHours = workingHours.toWorkingHours(),
        dayOffs = dayOffs.map { it.domain() }
    )

    fun domain(): BusinessSnapshot = BusinessSnapshot(
        id = id.value.toKotlinUuid(),
        name = name,
        address = address,
        timeZone = TimeZone.of(timezone),
        isEnabled = enabled,
        schedule = schedule()
    )

    private fun replaceSchedule(snapshot: BusinessSnapshot) {
        WorkingHourEntity.batchReplace(id.value, snapshot.schedule.workingHours())
        DayOffEntity.batchReplace(id.value, snapshot.schedule.dayOffs)
    }

    companion object : DecoratorUUIDEntityClass<AppointmentBusinessEntity>(AppointmentBusinessTable) {

        fun new(snapshot: BusinessSnapshot): AppointmentBusinessEntity = new(snapshot.id.toJavaUuid()) {
            name = snapshot.name
            address = snapshot.address
            timezone = snapshot.timeZone.id
            enabled = true
            workingDays = snapshot.schedule.activeDays().toWorkingDaysMask()
        }.apply { replaceSchedule(snapshot) }

        fun findByIdAndUpdate(snapshot: BusinessSnapshot, updatedAt: Instant) =
            findByIdAndUpdate(snapshot.id.toJavaUuid()) {
                val sourceUpdatedAt = it.sourceUpdatedAt
                if (sourceUpdatedAt == null || sourceUpdatedAt < updatedAt) {
                    it.name = snapshot.name
                    it.address = snapshot.address
                    it.timezone = snapshot.timeZone.id
                    it.workingDays = snapshot.schedule.activeDays().toWorkingDaysMask()
                    it.sourceUpdatedAt = updatedAt
                    it.updatedAt = Clock.System.now()
                    it.replaceSchedule(snapshot)
                }
            }
    }
}
