package com.bookk.appointments.domain.api.entity

import com.bookk.core.containedIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AppointmentSettings(
    val id: Uuid,
    val businessId: Uuid,
    val timeZone: TimeZone,
    val schedule: WorkingSchedule,
    val dayOffs: List<DayOffRange>,
    val automaticApproval: Boolean,
    val inBetweenBreakInMinutes: Int,
    val appointmentNote: String
) {
    companion object {
        fun stub(businessId: Uuid = Uuid.random()) = AppointmentSettings(businessId = businessId, TimeZone.of("UTC"))
    }

    constructor(businessId: Uuid, timeZone: TimeZone) : this(
        id = Uuid.random(),
        businessId = businessId,
        timeZone = timeZone,
        schedule = WorkingSchedule(),
        automaticApproval = false,
        dayOffs = listOf(),
        inBetweenBreakInMinutes = 10,
        appointmentNote = ""
    )

    fun isInWorkday(date: Instant): Boolean {
        val localDate = date.toLocalDateTime(timeZone)
        if (!schedule[localDate.dayOfWeek].isActive) return false
        return dayOffs.none { localDate.date in it.start..it.end }
    }

    fun isInWorktime(date: Instant, dateEnd: Instant): Boolean {
        val start = date.toLocalDateTime(timeZone)
        val startTime = start.time.toMillisecondOfDay()
        val endTime = dateEnd.toLocalDateTime(timeZone).time.toMillisecondOfDay()
        val dayOfWeek = start.dayOfWeek
        val schedule = schedule[dayOfWeek]
        return schedule.workingTime.any { time ->
            (startTime..endTime).containedIn(time.from.toMillisecondOfDay()..time.to.toMillisecondOfDay())
        }
    }
}
