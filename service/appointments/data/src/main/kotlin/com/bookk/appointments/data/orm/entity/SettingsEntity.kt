package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class SettingsEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    val business by AppointmentBusinessEntity referencedOn SettingsTable.businessId
    var workingDays by SettingsTable.workingDays
    val workingHours by WorkingHourEntity referrersOn WorkingHoursTable.settingsId
    val dayOffs by DayOffEntity referrersOn DayOffsTable.settingsId
    var automaticApproval by SettingsTable.automaticApproval
    var inBetweenBreakInMinutes by SettingsTable.inBetweenBreakInMinutes
    var appointmentNote by SettingsTable.appointmentNote

    fun domain(): AppointmentSettings = AppointmentSettings(
        id = id.value.toKotlinUuid(),
        businessId = business.id.value.toKotlinUuid(),
        timeZone = TimeZone.of(business.timezone),
        workingDays = buildList {
            DayOfWeek.entries.forEach {
                if (workingDays and (1 shl it.isoDayNumber).toByte() != 0.toByte()) {
                    add(it)
                }
            }
        },
        workingHours = workingHours.map {
            WorkHour(
                dayOfWeek = DayOfWeek(it.dayOfWeek.toInt()),
                from = it.startTime,
                to = it.endTime
            )
        },
        automaticApproval = automaticApproval,
        dayOffs = dayOffs.map { it.date },
        inBetweenBreakInMinutes = inBetweenBreakInMinutes,
        appointmentNote = appointmentNote,
    )

    companion object : DecoratorUUIDEntityClass<SettingsEntity>(SettingsTable) {
        fun new(settings: AppointmentSettings): SettingsEntity = new {
            workingDays = settings.workingDays.fold(0) { acc, day ->
                acc or (1 shl day.isoDayNumber).toByte()
            }
            inBetweenBreakInMinutes = settings.inBetweenBreakInMinutes
            appointmentNote = settings.appointmentNote
            automaticApproval = settings.automaticApproval
        }

        fun findByIdAndUpdate(settings: AppointmentSettings) = findByIdAndUpdate(settings.id.toJavaUuid()) {
            it.workingDays = settings.workingDays.fold(0) { acc, day ->
                acc or (1 shl day.isoDayNumber).toByte()
            }
            it.inBetweenBreakInMinutes = settings.inBetweenBreakInMinutes
            it.appointmentNote = settings.appointmentNote
            it.automaticApproval = settings.automaticApproval
        }
    }
}