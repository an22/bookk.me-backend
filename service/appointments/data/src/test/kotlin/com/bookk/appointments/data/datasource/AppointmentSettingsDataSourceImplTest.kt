package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.AppointmentSettingsUpdate
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.appointments.domain.api.entity.WorkingSchedule
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class AppointmentSettingsDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            AppointmentBusinessTable, SettingsTable, WorkingHoursTable, DayOffsTable
        )
        val sut = AppointmentSettingsDataSourceImpl()
        val subscriptionSut = AppointmentSubscriptionDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup(
            schedule: WorkingSchedule = WorkingSchedule(),
            dayOffs: List<DayOffRange> = emptyList()
        ) {
            val snapshot = BusinessSnapshot.stub(schedule = schedule, dayOffs = dayOffs)
            suspendTransaction { subscriptionSut.attachBusiness(snapshot) }
            businessId = snapshot.id
        }
    }

    @Test
    fun `should create settings and retrieve by business id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val settings = AppointmentSettings.stub(fixture.businessId)

        whenn()
        val created = suspendTransaction { fixture.sut.create(settings) }
        val found = suspendTransaction { fixture.sut.get(fixture.businessId) }

        then()
        assertNotNull(found)
        assertEquals(fixture.businessId, found!!.businessId)
        assertEquals(settings.inBetweenBreakInMinutes, created.inBetweenBreakInMinutes)
    }

    @Test
    fun `should return null when settings not found for business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val found = suspendTransaction { fixture.sut.get(fixture.businessId) }

        then()
        assertNull(found)
    }

    @Test
    fun `should update settings and persist changes`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(AppointmentSettings.stub(fixture.businessId)) }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.update(
                AppointmentSettingsUpdate.stub(
                    businessId = fixture.businessId,
                    inBetweenBreakInMinutes = 20,
                    appointmentNote = "Updated note"
                )
            )
        }

        then()
        assertEquals(20, updated.inBetweenBreakInMinutes)
        assertEquals("Updated note", updated.appointmentNote)
    }

    @Test
    fun `should expose the business schedule on the settings`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val schedule = WorkingSchedule(
            workingDays = listOf(DayOfWeek.SATURDAY),
            workingHours = mapOf(
                DayOfWeek.SATURDAY to listOf(WorkHour(DayOfWeek.SATURDAY, LocalTime(10, 0), LocalTime(14, 0)))
            )
        )
        val dayOffs = listOf(DayOffRange(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31)))
        fixture.setup(schedule = schedule, dayOffs = dayOffs)
        suspendTransaction { fixture.sut.create(AppointmentSettings.stub(fixture.businessId)) }

        whenn()
        val found = suspendTransaction { fixture.sut.get(fixture.businessId) }

        then()
        assertEquals(listOf(DayOfWeek.SATURDAY), found!!.schedule.activeDays())
        assertEquals(
            listOf(WorkHour(DayOfWeek.SATURDAY, LocalTime(10, 0), LocalTime(14, 0))),
            found.schedule[DayOfWeek.SATURDAY].workingTime
        )
        assertEquals(dayOffs, found.dayOffs)
    }

    @Test
    fun `should keep the business schedule when updating settings`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction { fixture.sut.create(AppointmentSettings.stub(fixture.businessId)) }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.update(
                AppointmentSettingsUpdate.stub(businessId = fixture.businessId, appointmentNote = "Updated note")
            )
        }

        then()
        assertEquals(created.schedule.activeDays().sorted(), updated.schedule.activeDays().sorted())
        assertEquals(
            created.schedule[DayOfWeek.MONDAY].workingTime,
            updated.schedule[DayOfWeek.MONDAY].workingTime
        )
    }

    @Test
    fun `should retrieve settings for update with lock`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        suspendTransaction { fixture.sut.create(AppointmentSettings.stub(fixture.businessId)) }

        whenn()
        val found = suspendTransaction { fixture.sut.getForUpdate(fixture.businessId) }

        then()
        assertNotNull(found)
        assertEquals(fixture.businessId, found!!.businessId)
    }

    @Test
    fun `should return null from getForUpdate when settings not found`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val found = suspendTransaction { fixture.sut.getForUpdate(fixture.businessId) }

        then()
        assertNull(found)
    }
}
