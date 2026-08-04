package library.schedule

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DayOffRange(
    val start: LocalDate,
    val end: LocalDate
)
