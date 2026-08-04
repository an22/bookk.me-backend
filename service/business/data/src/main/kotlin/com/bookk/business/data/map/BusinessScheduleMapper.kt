package com.bookk.business.data.map

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlin.experimental.and
import kotlin.experimental.or

internal fun List<DayOfWeek>.toWorkingDaysMask(): Byte =
    fold(0) { acc, day -> acc or (1 shl day.isoDayNumber).toByte() }

internal fun Byte.toWorkingDays(): List<DayOfWeek> =
    DayOfWeek.entries.filter { this and (1 shl it.isoDayNumber).toByte() != 0.toByte() }
