package com.bookk.appointments.domain.api.entity

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class WorkHour(
    val dayOfWeek: DayOfWeek,
    val from: LocalTime,
    val to: LocalTime
) {
    companion object {
        fun DayOfWeek.nineToFive() = WorkHour(
            dayOfWeek = this,
            from = LocalTime(9, 0),
            to = LocalTime(17, 0)
        )
    }
}