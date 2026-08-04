package library.schedule

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
                workingTime = listOf(WorkHour.NINE_TO_FIVE),
                isActive = dayOfWeek < DayOfWeek.SATURDAY
            )
        }
    }
}
