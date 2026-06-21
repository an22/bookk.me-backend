package com.bookk.appointments.domain.api.entity

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
data class WorkingSchedule(
    val days: Map<DayOfWeek, DayOfWeekSchedule>
) {

    constructor() : this(
        days = DayOfWeek.entries.associateWith { DayOfWeekSchedule.default(it) }
    )

    constructor(
        workingDays: List<DayOfWeek>,
        workingHours: Map<DayOfWeek, List<WorkHour>>
    ) : this(
        days = DayOfWeek.entries.associateWith { day ->
            DayOfWeekSchedule(
                workingTime = workingHours[day].orEmpty(),
                isActive = workingDays.contains(day)
            )
        }
    )

    operator fun get(dayOfWeek: DayOfWeek): DayOfWeekSchedule = days[dayOfWeek] ?: DayOfWeekSchedule.default(dayOfWeek)

    fun list(): List<DayOfWeekSchedule> = days.values.toList()

    fun activeDays(): List<DayOfWeek> = days.filterValues { it.isActive }.keys.toList()
}