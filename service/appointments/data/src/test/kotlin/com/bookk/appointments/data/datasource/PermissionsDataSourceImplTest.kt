package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.UserHasAppointmentPermissions
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class PermissionsDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(AppointmentBusinessTable, WorkingHoursTable, DayOffsTable, UserHasAppointmentPermissions)
        val sut = PermissionsDataSourceImpl()
        val subscriptionSut = AppointmentSubscriptionDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup() {
            val snapshot = BusinessSnapshot.stub()
            suspendTransaction { subscriptionSut.attachBusiness(snapshot) }
            businessId = snapshot.id
        }
    }

    @Test
    fun `should return null when permissions not set`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val permissions = suspendTransaction { fixture.sut.getPermissions(Uuid.random(), fixture.businessId) }

        then()
        assertNull(permissions)
    }

    @Test
    fun `should create a permission row when setting permissions for the first time`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()

        whenn()
        suspendTransaction { fixture.sut.setPermissions(userId, fixture.businessId, 1) }
        val permissions = suspendTransaction { fixture.sut.getPermissions(userId, fixture.businessId) }

        then()
        assertEquals(1, permissions)
    }

    @Test
    fun `should overwrite an existing permission when set again`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        suspendTransaction { fixture.sut.setPermissions(userId, fixture.businessId, 1) }

        whenn()
        suspendTransaction { fixture.sut.setPermissions(userId, fixture.businessId, 2) }
        val permissions = suspendTransaction { fixture.sut.getPermissions(userId, fixture.businessId) }

        then()
        assertEquals(2, permissions)
    }

    @Test
    fun `should delete permissions for a user across businesses`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        suspendTransaction { fixture.sut.setPermissions(userId, fixture.businessId, 7) }

        whenn()
        suspendTransaction { fixture.sut.deleteForUser(userId) }
        val permissions = suspendTransaction { fixture.sut.getPermissions(userId, fixture.businessId) }

        then()
        assertNull(permissions)
    }

    @Test
    fun `should not fail deleting permissions for a user with none`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.deleteForUser(Uuid.random()) } }

        then()
        assertTrue(result.isSuccess)
    }
}
