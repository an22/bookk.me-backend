package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AppointmentSubscriptionDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(AppointmentBusinessTable)
        val sut = AppointmentSubscriptionDataSourceImpl()
    }

    @Test
    fun `should attach business and retrieve snapshot`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val snapshot = BusinessSnapshot.stub()

        whenn()
        suspendTransaction { fixture.sut.attachBusiness(snapshot) }
        val found = suspendTransaction { fixture.sut.getBusinessSnapshot(snapshot.id) }

        then()
        assertNotNull(found)
        assertEquals(snapshot.id, found!!.id)
        assertEquals(snapshot.name, found.name)
        assertTrue(found.isEnabled)
    }

    @Test
    fun `should return null snapshot for unknown business`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val found = suspendTransaction { fixture.sut.getBusinessSnapshot(BusinessSnapshot.stub().id) }

        then()
        assertNull(found)
    }

    @Test
    fun `should update business info`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val snapshot = BusinessSnapshot.stub()
        suspendTransaction { fixture.sut.attachBusiness(snapshot) }

        whenn()
        suspendTransaction {
            fixture.sut.updateBusinessInfo(snapshot.id, "New Name", "New Address", TimeZone.of("Europe/Kyiv"))
        }
        val updated = suspendTransaction { fixture.sut.getBusinessSnapshot(snapshot.id) }

        then()
        assertNotNull(updated)
        assertEquals("New Name", updated!!.name)
        assertEquals("New Address", updated.address)
    }

    @Test
    fun `should update full business snapshot`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val snapshot = BusinessSnapshot.stub()
        suspendTransaction { fixture.sut.attachBusiness(snapshot) }
        val modified = snapshot.copy(name = "Modified", isEnabled = false)

        whenn()
        suspendTransaction { fixture.sut.updateBusiness(modified) }
        val updated = suspendTransaction { fixture.sut.getBusinessSnapshot(snapshot.id) }

        then()
        assertEquals("Modified", updated!!.name)
        assertFalse(updated.isEnabled)
    }

    @Test
    fun `should enable and disable business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val snapshot = BusinessSnapshot.stub()
        suspendTransaction { fixture.sut.attachBusiness(snapshot) }

        whenn()
        suspendTransaction { fixture.sut.disableBusiness(snapshot.id) }
        val disabledCheck = suspendTransaction { fixture.sut.isBusinessEnabled(snapshot.id) }

        suspendTransaction { fixture.sut.enableBusiness(snapshot.id) }
        val enabledCheck = suspendTransaction { fixture.sut.isBusinessEnabled(snapshot.id) }

        then()
        assertFalse(disabledCheck)
        assertTrue(enabledCheck)
    }

    @Test
    fun `should return false for unknown business enabled check`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val snapshot = BusinessSnapshot.stub()

        whenn()
        val enabled = suspendTransaction { fixture.sut.isBusinessEnabled(snapshot.id) }

        then()
        assertFalse(enabled)
    }

    @Test
    fun `should detach business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val snapshot = BusinessSnapshot.stub()
        suspendTransaction { fixture.sut.attachBusiness(snapshot) }

        whenn()
        suspendTransaction { fixture.sut.detachBusiness(snapshot.id) }
        val found = suspendTransaction { fixture.sut.getBusinessSnapshot(snapshot.id) }

        then()
        assertNull(found)
    }
}
