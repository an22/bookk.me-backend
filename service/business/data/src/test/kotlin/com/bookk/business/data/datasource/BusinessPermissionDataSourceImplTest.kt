package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionGrantsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.TimeZone
import library.permissions.ResourcePermission
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class BusinessPermissionDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable,
            BusinessDashboardTable,
            BusinessPermissionGrantsTable,
            BusinessWorkingHoursTable,
            BusinessDayOffTable
        )
        val businessDataSource = BusinessDataSourceImpl()
        val sut = BusinessPermissionDataSourceImpl()
    }

    @Test
    fun `should set and retrieve a resource permission`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }
        val permission = ResourcePermission(view = true, update = true)

        whenn()
        suspendTransaction { fixture.sut.setPermission(userId, created.id, BusinessResource.CLIENTS, permission) }
        val stored = suspendTransaction { fixture.sut.getPermission(userId, created.id, BusinessResource.CLIENTS) }

        then()
        assertEquals(permission, stored)
    }

    @Test
    fun `should overwrite an existing permission when set again`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }
        suspendTransaction { fixture.sut.setPermission(userId, created.id, BusinessResource.CLIENTS, ResourcePermission(view = true)) }

        whenn()
        suspendTransaction { fixture.sut.setPermission(userId, created.id, BusinessResource.CLIENTS, ResourcePermission.FULL) }
        val stored = suspendTransaction { fixture.sut.getPermission(userId, created.id, BusinessResource.CLIENTS) }

        then()
        assertEquals(ResourcePermission.FULL, stored)
    }

    @Test
    fun `should return none permission when not set`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }

        whenn()
        val permission = suspendTransaction { fixture.sut.getPermission(Uuid.random(), created.id, BusinessResource.CLIENTS) }

        then()
        assertEquals(ResourcePermission.NONE, permission)
    }

    @Test
    fun `should keep resource grants independent from one another`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }
        suspendTransaction { fixture.sut.setPermission(userId, created.id, BusinessResource.CLIENTS, ResourcePermission.FULL) }

        whenn()
        val employees = suspendTransaction { fixture.sut.getPermission(userId, created.id, BusinessResource.EMPLOYEES) }

        then()
        assertEquals(ResourcePermission.NONE, employees)
    }

    @Test
    fun `should aggregate every resource into a single permissions snapshot`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val created = suspendTransaction { fixture.businessDataSource.createBusiness(userId, "Salon", "USD", TimeZone.UTC) }
        suspendTransaction {
            fixture.sut.setPermission(userId, created.id, BusinessResource.CLIENTS, ResourcePermission(view = true))
            fixture.sut.setPermission(userId, created.id, BusinessResource.SERVICES, ResourcePermission.FULL)
        }

        whenn()
        val permissions = suspendTransaction { fixture.sut.getPermissions(userId, created.id) }

        then()
        assertEquals(ResourcePermission(view = true), permissions.clients)
        assertEquals(ResourcePermission.FULL, permissions.services)
        assertEquals(ResourcePermission.NONE, permissions.business)
        assertEquals(ResourcePermission.NONE, permissions.employees)
        assertEquals(ResourcePermission.NONE, permissions.appointments)
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
            fixture.sut.setPermission(userId, ownBusiness.id, BusinessResource.CLIENTS, ResourcePermission.FULL)
            fixture.sut.setPermission(userId, otherBusiness.id, BusinessResource.CLIENTS, ResourcePermission(view = true))
        }

        whenn()
        suspendTransaction { fixture.sut.deleteUserPermissions(userId) }

        then()
        assertEquals(
            ResourcePermission.NONE,
            suspendTransaction { fixture.sut.getPermission(userId, ownBusiness.id, BusinessResource.CLIENTS) }
        )
        assertEquals(
            ResourcePermission.NONE,
            suspendTransaction { fixture.sut.getPermission(userId, otherBusiness.id, BusinessResource.CLIENTS) }
        )
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
