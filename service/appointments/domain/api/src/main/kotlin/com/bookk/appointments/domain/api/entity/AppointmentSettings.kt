package com.bookk.appointments.domain.api.entity

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
class AppointmentSettings(
    val id: Uuid,
    val businessId: Uuid,
    val timeZone: TimeZone,
    val workingDays: List<DayOfWeek>,
    val workingHours: List<WorkHour>,
    val dayOffs: List<LocalDate>,
    val inBetweenBreakInMinutes: Int,
    val appointmentNote: String
) {
    fun isInWorkday(date: Instant): Boolean {
        return true
    }

    fun isInWorktime(date: Instant): Boolean {
        return true
    }
}

@Serializable
class WorkHour(
    val dayOfWeek: DayOfWeek,
    val from: LocalTime,
    val to: LocalTime
)