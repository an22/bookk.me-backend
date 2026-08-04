package com.bookk.business.domain.api.business.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DayOffRange(
    val start: LocalDate,
    val end: LocalDate
)
