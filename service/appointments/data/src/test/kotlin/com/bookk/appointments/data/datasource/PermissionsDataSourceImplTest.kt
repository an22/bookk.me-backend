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
    fun `should init permissions and retrieve them`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()

        whenn()
        suspendTransaction { fixture.sut.initPermissions(userId, fixture.businessId, 7) }
        val permissions = suspendTransaction { fixture.sut.getPermissions(userId, fixture.businessId) }

        then()
        assertEquals(7, permissions)
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
    fun `should ignore duplicate init permissions call`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        suspendTransaction { fixture.sut.initPermissions(userId, fixture.businessId, 7) }

        whenn()
        suspendTransaction { fixture.sut.initPermissions(userId, fixture.businessId, 3) }
        val permissions = suspendTransaction { fixture.sut.getPermissions(userId, fixture.businessId) }

        then()
        assertEquals(7, permissions)
    }
}
