package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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

        suspend fun setup() {
            val snapshot = BusinessSnapshot.stub()
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
        val settings = AppointmentSettings.stub(fixture.businessId)
        val created = suspendTransaction { fixture.sut.create(settings) }

        whenn()
        val modified = created.copy(inBetweenBreakInMinutes = 20, appointmentNote = "Updated note")
        val updated = suspendTransaction { fixture.sut.update(modified) }

        then()
        assertEquals(20, updated.inBetweenBreakInMinutes)
        assertEquals("Updated note", updated.appointmentNote)
    }

    @Test
    fun `should retrieve settings for update with lock`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val settings = AppointmentSettings.stub(fixture.businessId)
        suspendTransaction { fixture.sut.create(settings) }

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

    @Test
    fun `should delete past day offs`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val pastDayOff = DayOffRange(LocalDate(2020, 1, 1), LocalDate(2020, 1, 2))
        val settings = AppointmentSettings.stub(fixture.businessId).copy(dayOffs = listOf(pastDayOff))
        suspendTransaction { fixture.sut.create(settings) }

        whenn()
        suspendTransaction { fixture.sut.deleteDayOffsInThePast() }

        then()
        val found = suspendTransaction { fixture.sut.get(fixture.businessId) }
        assertNotNull(found)
        assertTrue(found!!.dayOffs.isEmpty())
    }

    @Test
    fun `should preserve future day offs when deleting past ones`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val pastDayOff = DayOffRange(LocalDate(2020, 1, 1), LocalDate(2020, 1, 2))
        val futureDayOff = DayOffRange(LocalDate(2099, 12, 30), LocalDate(2099, 12, 31))
        val settings = AppointmentSettings.stub(fixture.businessId).copy(dayOffs = listOf(pastDayOff, futureDayOff))
        suspendTransaction { fixture.sut.create(settings) }

        whenn()
        suspendTransaction { fixture.sut.deleteDayOffsInThePast() }

        then()
        val found = suspendTransaction { fixture.sut.get(fixture.businessId) }
        assertNotNull(found)
        assertEquals(1, found!!.dayOffs.size)
        assertEquals(futureDayOff, found.dayOffs.first())
    }
}
