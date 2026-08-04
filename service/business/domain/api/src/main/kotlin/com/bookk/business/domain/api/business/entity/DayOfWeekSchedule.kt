package com.bookk.business.domain.api.business.entity

import com.bookk.business.domain.api.business.entity.WorkHour.Companion.nineToFive
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
