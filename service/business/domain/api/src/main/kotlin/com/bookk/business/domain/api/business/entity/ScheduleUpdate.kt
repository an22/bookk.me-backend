package com.bookk.business.domain.api.business.entity

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleUpdate(
    val workingSchedule: WorkingSchedule,
    val dayOffs: List<DayOffRange>
)
