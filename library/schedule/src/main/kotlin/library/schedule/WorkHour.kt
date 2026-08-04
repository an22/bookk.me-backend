package library.schedule

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class WorkHour(
    val from: LocalTime,
    val to: LocalTime
) {
    companion object {
        val NINE_TO_FIVE = WorkHour(
            from = LocalTime(9, 0),
            to = LocalTime(17, 0)
        )
    }
}
