package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class BusinessPermissionDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable,
            BusinessDashboardTable,
            BusinessPermissionsTable,
            BusinessWorkingHoursTable,
            BusinessDayOffTable
        )
        val businessDataSource = BusinessDataSourceImpl()
        val sut = BusinessPermissionDataSourceImpl()
    }

    @Test
    fun `should set and retrieve user permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        suspendTransaction { fixture.sut.setUserPermissions(userId, created.id, 7) }
        val permission = suspendTransaction { fixture.sut.getPermission(userId, created.id) }

        then()
        assertEquals(7, permission)
    }

    @Test
    fun `should return null permission when not set`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val permission = suspendTransaction { fixture.sut.getPermission(Uuid.random(), created.id) }

        then()
        assertNull(permission)
    }

    @Test
    fun `should delete user permissions across businesses`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val ownBusiness = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }
        val otherBusiness = suspendTransaction {
            fixture.businessDataSource.createBusiness(Uuid.random(), "Other Salon", "USD", TimeZone.UTC)
        }
        suspendTransaction {
            fixture.sut.setUserPermissions(userId, ownBusiness.id, 7)
            fixture.sut.setUserPermissions(userId, otherBusiness.id, 1)
        }

        whenn()
        suspendTransaction { fixture.sut.deleteUserPermissions(userId) }

        then()
        assertNull(suspendTransaction { fixture.sut.getPermission(userId, ownBusiness.id) })
        assertNull(suspendTransaction { fixture.sut.getPermission(userId, otherBusiness.id) })
    }

    @Test
    fun `should not fail deleting permissions for a user with none`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val result = runCatching { suspendTransaction { fixture.sut.deleteUserPermissions(Uuid.random()) } }

        then()
        assertTrue(result.isSuccess)
    }
}
