package com.bookk.appointments.domain.api.entity

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
    val workingDays: List<DayOfWeek>,
    val workingHours: List<WorkHour>,
    val dayOffs: List<LocalDate>,
    val automaticApproval: Boolean,
    val inBetweenBreakInMinutes: Int,
    val appointmentNote: String
) {
    companion object {
        fun stub(businessId: Uuid = Uuid.random()) = AppointmentSettings(businessId = businessId)
    }

    constructor(businessId: Uuid) : this(
        id = Uuid.random(),
        businessId = businessId,
        timeZone = TimeZone.of("UTC"),
        workingDays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
        ),
        workingHours = listOf(
            DayOfWeek.MONDAY.nineToFive(),
            DayOfWeek.TUESDAY.nineToFive(),
            DayOfWeek.WEDNESDAY.nineToFive(),
            DayOfWeek.THURSDAY.nineToFive(),
            DayOfWeek.FRIDAY.nineToFive(),
        ),
        automaticApproval = false,
        dayOffs = listOf(),
        inBetweenBreakInMinutes = 10,
        appointmentNote = ""
    )

    fun isInWorkday(date: Instant): Boolean {
        return date.toLocalDateTime(timeZone).dayOfWeek in workingDays
    }

    fun isInWorktime(date: Instant): Boolean {
        val localDateTime = date.toLocalDateTime(timeZone)
        val dayOfWeek = localDateTime.dayOfWeek
        val workTime = workingHours.firstOrNull { it.dayOfWeek == dayOfWeek } ?: return false
        return localDateTime.time in workTime.from..workTime.to
    }
}

@Serializable
class WorkHour(
    val dayOfWeek: DayOfWeek,
    val from: LocalTime,
    val to: LocalTime
)

fun DayOfWeek.nineToFive() = WorkHour(
    dayOfWeek = this,
    from = LocalTime(9, 0),
    to = LocalTime(17, 0)
)
