package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
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
import kotlin.uuid.Uuid

internal class BusinessDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(BusinessTable, BusinessDashboardTable, BusinessPermissionsTable)
        val sut = BusinessDataSourceImpl()
    }

    @Test
    fun `should create business and retrieve by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()

        whenn()
        val created = suspendTransaction {
            fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC)
        }
        val found = suspendTransaction { fixture.sut.getBusinessById(created.id) }

        then()
        assertNotNull(found)
        assertEquals("Salon", found!!.name)
        assertEquals("USD", found.currencyCode)
    }

    @Test
    fun `should return null when business not found`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val found = suspendTransaction { fixture.sut.getBusinessById(Uuid.random()) }

        then()
        assertNull(found)
    }

    @Test
    fun `should report business exists after creation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val exists = suspendTransaction { fixture.sut.isBusinessExist(userId) }

        then()
        assertTrue(exists)
    }

    @Test
    fun `should report business does not exist for unknown user`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val exists = suspendTransaction { fixture.sut.isBusinessExist(Uuid.random()) }

        then()
        assertFalse(exists)
    }

    // updateBusiness uses updateReturning which is not supported by Exposed's H2 dialect

    @Test
    fun `should retrieve dashboard business for user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Dashboard Salon", "USD", TimeZone.UTC) }

        whenn()
        val dashboard = suspendTransaction { fixture.sut.getDashboardBusiness(userId) }

        then()
        assertNotNull(dashboard)
        assertEquals(created.id, dashboard!!.id)
    }

    @Test
    fun `should return null dashboard for unknown user`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val dashboard = suspendTransaction { fixture.sut.getDashboardBusiness(Uuid.random()) }

        then()
        assertNull(dashboard)
    }

    @Test
    fun `should get user businesses with dashboard id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val result = suspendTransaction { fixture.sut.getUserBusinesses(userId) }

        then()
        assertEquals(1, result.businesses.size)
        assertEquals(created.id, result.dashboardId)
    }

    @Test
    fun `should set and retrieve user permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

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
        val created = suspendTransaction { fixture.sut.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val permission = suspendTransaction { fixture.sut.getPermission(Uuid.random(), created.id) }

        then()
        assertNull(permission)
    }

    // deleteUserBusinesses uses deleteReturning which is not supported by Exposed's H2 dialect
}
