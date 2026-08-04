package com.bookk.appointments.data.map

import com.bookk.appointments.domain.api.entity.WorkingSchedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlin.experimental.and
import kotlin.experimental.or

internal fun WorkingSchedule.toWorkingDaysMask(): Byte = activeDays()
    .fold(0) { acc, day -> acc or (1 shl day.isoDayNumber).toByte() }

internal fun Byte.toWorkingDays(): List<DayOfWeek> =
    DayOfWeek.entries.filter { this and (1 shl it.isoDayNumber).toByte() != 0.toByte() }
