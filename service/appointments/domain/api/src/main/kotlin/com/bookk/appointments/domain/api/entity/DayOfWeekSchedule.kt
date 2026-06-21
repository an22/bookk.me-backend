package com.bookk.appointments.domain.api.entity

import com.bookk.appointments.domain.api.entity.WorkHour.Companion.nineToFive
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
data class DayOfWeekSchedule(
    val workingTime: List<WorkHour>,
    val isActive: Boolean
) {
    companion object {
        fun default(dayOfWeek: DayOfWeek): DayOfWeekSchedule {
            return DayOfWeekSchedule(
                workingTime = listOf(dayOfWeek.nineToFive()),
                isActive = dayOfWeek < DayOfWeek.SATURDAY
            )
        }
    }
}