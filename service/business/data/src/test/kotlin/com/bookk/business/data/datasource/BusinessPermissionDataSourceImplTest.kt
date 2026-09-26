package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionGrantsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.BusinessWorkingHoursTable
import com.bookk.business.data.orm.table.EmployeeCanProvideServiceTable
import com.bookk.business.data.orm.table.EmployeeDayOffTable
import com.bookk.business.data.orm.table.EmployeeTable
import com.bookk.business.data.orm.table.EmployeeWorkingHoursTable
import com.bookk.business.data.orm.table.ServiceGroupTable
import com.bookk.business.data.orm.table.ServiceTable
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import kotlinx.datetime.TimeZone
import library.permissions.EmployeeAccessSuspended
import library.permissions.ResourcePermission
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class BusinessPermissionDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable,
            BusinessDashboardTable,
            BusinessWorkingHoursTable,
            BusinessDayOffTable,
            ServiceGroupTable,
            ServiceTable,
            EmployeeTable,
            EmployeeCanProvideServiceTable,
            EmployeeWorkingHoursTable,
            EmployeeDayOffTable,
            BusinessPermissionGrantsTable
        )
        val businessDataSource = BusinessDataSourceImpl()
        val employeeDataSource = EmployeeDataSourceImpl()
        val sut = BusinessPermissionDataSourceImpl()

        suspend fun employee(userId: Uuid, businessName: String = "Salon"): Employee {
            val business = suspendTransaction { businessDataSource.createBusiness(Uuid.random(), businessName, "USD", TimeZone.UTC) }
            return suspendTransaction { employeeDataSource.createEmployee(Employee.stub(businessId = business.id, userId = userId)) }
        }
    }

    @Test
    fun `should set and retrieve a resource permission`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        val permission = ResourcePermission(view = true, update = true, delete = false)

        whenn()
        suspendTransaction { fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to permission)) }
        val stored = suspendTransaction { fixture.sut.getPermission(employee.userId, employee.businessId, BusinessResource.CLIENTS) }

        then()
        assertEquals(permission, stored)
    }

    @Test
    fun `should overwrite an existing permission when set again`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction { fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission(view = true, update = false, delete = false))) }

        whenn()
        suspendTransaction { fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL)) }
        val stored = suspendTransaction { fixture.sut.getPermission(employee.userId, employee.businessId, BusinessResource.CLIENTS) }

        then()
        assertEquals(ResourcePermission.FULL, stored)
    }

    @Test
    fun `should set several resources at once and leave the rest untouched`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction { fixture.sut.setPermissions(employee, mapOf(BusinessResource.BUSINESS to ResourcePermission(view = true, update = false, delete = false))) }

        whenn()
        suspendTransaction {
            fixture.sut.setPermissions(
                employee,
                mapOf(
                    BusinessResource.CLIENTS to ResourcePermission.FULL,
                    BusinessResource.SERVICES to ResourcePermission(view = true, update = true, delete = false)
                )
            )
        }
        val permissions = suspendTransaction { fixture.sut.getPermissions(employee.userId, employee.businessId) }

        then()
        assertEquals(
            BusinessPermissions.stub(
                business = ResourcePermission(view = true, update = false, delete = false),
                clients = ResourcePermission.FULL,
                services = ResourcePermission(view = true, update = true, delete = false)
            ),
            permissions
        )
    }

    @Test
    fun `should return none permission when not set`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())

        whenn()
        val permission = suspendTransaction { fixture.sut.getPermission(Uuid.random(), employee.businessId, BusinessResource.CLIENTS) }

        then()
        assertEquals(ResourcePermission.NONE, permission)
    }

    @Test
    fun `should keep resource grants independent from one another`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction { fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL)) }

        whenn()
        val employees = suspendTransaction { fixture.sut.getPermission(employee.userId, employee.businessId, BusinessResource.EMPLOYEES) }

        then()
        assertEquals(ResourcePermission.NONE, employees)
    }

    @Test
    fun `should aggregate every resource into a single permissions snapshot`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction {
            fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission(view = true, update = false, delete = false)))
            fixture.sut.setPermissions(employee, mapOf(BusinessResource.SERVICES to ResourcePermission.FULL))
        }

        whenn()
        val permissions = suspendTransaction { fixture.sut.getPermissions(employee.userId, employee.businessId) }

        then()
        assertEquals(ResourcePermission(view = true, update = false, delete = false), permissions.clients)
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
        val ownEmployee = fixture.employee(userId, businessName = "Salon")
        val otherEmployee = fixture.employee(userId, businessName = "Other Salon")
        suspendTransaction {
            fixture.sut.setPermissions(ownEmployee, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL))
            fixture.sut.setPermissions(otherEmployee, mapOf(BusinessResource.CLIENTS to ResourcePermission(view = true, update = false, delete = false)))
        }

        whenn()
        suspendTransaction { fixture.sut.deleteUserPermissions(userId) }

        then()
        assertEquals(
            ResourcePermission.NONE,
            suspendTransaction { fixture.sut.getPermission(userId, ownEmployee.businessId, BusinessResource.CLIENTS) }
        )
        assertEquals(
            ResourcePermission.NONE,
            suspendTransaction { fixture.sut.getPermission(userId, otherEmployee.businessId, BusinessResource.CLIENTS) }
        )
    }

    @Test
    fun `should delete employee grants when the employee is deleted`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction { fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL)) }

        whenn()
        suspendTransaction { fixture.employeeDataSource.deleteEmployee(employee.businessId, employee.id) }
        val stored = suspendTransaction { fixture.sut.getPermission(employee.userId, employee.businessId, BusinessResource.CLIENTS) }

        then()
        assertEquals(ResourcePermission.NONE, stored)
    }

    @Test
    fun `should reject a permission check while the employee is suspended`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction {
            fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL))
            fixture.employeeDataSource.setSuspendedAt(employee.id, Instant.fromEpochMilliseconds(1))
        }

        whenn()
        val result = runCatching {
            suspendTransaction { fixture.sut.getPermission(employee.userId, employee.businessId, BusinessResource.CLIENTS) }
        }

        then()
        assertTrue(result.exceptionOrNull() is EmployeeAccessSuspended)
    }

    @Test
    fun `should return no permissions snapshot while the employee is suspended`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction {
            fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL))
            fixture.employeeDataSource.setSuspendedAt(employee.id, Instant.fromEpochMilliseconds(1))
        }

        whenn()
        val permissions = suspendTransaction { fixture.sut.getPermissions(employee.userId, employee.businessId) }

        then()
        assertEquals(BusinessPermissions.NONE, permissions)
    }

    @Test
    fun `should restore the stored permission once the employee is reinstated`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = fixture.employee(Uuid.random())
        suspendTransaction {
            fixture.sut.setPermissions(employee, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL))
            fixture.employeeDataSource.setSuspendedAt(employee.id, Instant.fromEpochMilliseconds(1))
        }

        whenn()
        suspendTransaction { fixture.employeeDataSource.setSuspendedAt(employee.id, null) }
        val stored = suspendTransaction { fixture.sut.getPermission(employee.userId, employee.businessId, BusinessResource.CLIENTS) }

        then()
        assertEquals(ResourcePermission.FULL, stored)
    }

    @Test
    fun `should keep the same user's grants in another business when suspended in one`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val suspended = fixture.employee(userId, businessName = "Salon")
        val active = fixture.employee(userId, businessName = "Other Salon")
        suspendTransaction {
            fixture.sut.setPermissions(suspended, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL))
            fixture.sut.setPermissions(active, mapOf(BusinessResource.CLIENTS to ResourcePermission.FULL))
            fixture.employeeDataSource.setSuspendedAt(suspended.id, Instant.fromEpochMilliseconds(1))
        }

        whenn()
        val stored = suspendTransaction { fixture.sut.getPermission(userId, active.businessId, BusinessResource.CLIENTS) }

        then()
        assertEquals(ResourcePermission.FULL, stored)
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
