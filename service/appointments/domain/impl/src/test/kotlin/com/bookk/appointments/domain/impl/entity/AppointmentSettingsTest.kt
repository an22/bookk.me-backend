package com.bookk.appointments.domain.impl.entity

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class AppointmentSettingsTest {

    private fun settingsWithDayOffs(dayOffs: List<DayOffRange>) = AppointmentSettings(
        id = Uuid.random(),
        businessId = Uuid.random(),
        timeZone = TimeZone.of("UTC"),
        workingDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
        workingHours = listOf(),
        dayOffs = dayOffs,
        automaticApproval = false,
        inBetweenBreakInMinutes = 10,
        appointmentNote = ""
    )

    @Test
    fun `should return true when date is a working day with no day-offs`() = runUnitTest {
        given()
        val settings = settingsWithDayOffs(dayOffs = listOf())
        val monday = LocalDate(2026, 6, 22).atStartOfDayIn(settings.timeZone)

        whenn()
        val result = settings.isInWorkday(monday)

        then()
        assertTrue(result)
    }

    @Test
    fun `should return false when date falls inside a day-off range`() = runUnitTest {
        given()
        val range = DayOffRange(start = LocalDate(2026, 6, 22), end = LocalDate(2026, 6, 26))
        val settings = settingsWithDayOffs(dayOffs = listOf(range))
        val wednesday = LocalDate(2026, 6, 24).atStartOfDayIn(settings.timeZone)

        whenn()
        val result = settings.isInWorkday(wednesday)

        then()
        assertFalse(result)
    }

    @Test
    fun `should return false when date is the boundary of a day-off range`() = runUnitTest {
        given()
        val range = DayOffRange(start = LocalDate(2026, 6, 22), end = LocalDate(2026, 6, 26))
        val settings = settingsWithDayOffs(dayOffs = listOf(range))
        val rangeEnd = LocalDate(2026, 6, 26).atStartOfDayIn(settings.timeZone)

        whenn()
        val result = settings.isInWorkday(rangeEnd)

        then()
        assertFalse(result)
    }

    @Test
    fun `should return true when date is outside any day-off range`() = runUnitTest {
        given()
        val range = DayOffRange(start = LocalDate(2026, 6, 22), end = LocalDate(2026, 6, 24))
        val settings = settingsWithDayOffs(dayOffs = listOf(range))
        val friday = LocalDate(2026, 6, 26).atStartOfDayIn(settings.timeZone)

        whenn()
        val result = settings.isInWorkday(friday)

        then()
        assertTrue(result)
    }

    @Test
    fun `should return false when date is not a working day regardless of day-offs`() = runUnitTest {
        given()
        val settings = settingsWithDayOffs(dayOffs = listOf())
        val saturday = LocalDate(2026, 6, 27).atStartOfDayIn(settings.timeZone)

        whenn()
        val result = settings.isInWorkday(saturday)

        then()
        assertFalse(result)
    }
}
