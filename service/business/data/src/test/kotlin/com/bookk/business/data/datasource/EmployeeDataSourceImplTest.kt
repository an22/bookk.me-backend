package com.bookk.business.data.datasource

import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.data.orm.table.EmployeeCanProvideServiceTable
import com.bookk.business.data.orm.table.EmployeeTable
import com.bookk.business.data.orm.table.ServiceGroupTable
import com.bookk.business.data.orm.table.ServiceTable
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.entity.ServiceGroup
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

internal class EmployeeDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(
            BusinessTable, BusinessDashboardTable, BusinessPermissionsTable,
            ServiceGroupTable, ServiceTable, EmployeeTable, EmployeeCanProvideServiceTable
        )
        val sut = EmployeeDataSourceImpl()
        val businessSut = BusinessDataSourceImpl()
        val serviceSut = ServiceDataSourceImpl()
        lateinit var businessId: Uuid

        suspend fun setup() {
            businessId = suspendTransaction {
                businessSut.createBusiness(Uuid.random(), "Test Business", "USD", TimeZone.UTC)
            }.id
        }

        suspend fun createService(name: String = "stub-service"): Service {
            val group = suspendTransaction {
                serviceSut.createServiceGroup(ServiceGroup.stub(businessId = businessId, name = "group-$name"))
            }
            return suspendTransaction {
                serviceSut.createService(Service.stub(businessId = businessId, group = group, name = name))
            }
        }
    }

    @Test
    fun `should create employee and retrieve by business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = Employee.stub(
            businessId = fixture.businessId,
            name = "Alice",
            lastName = "Smith",
            phone = "+1234567890",
            email = "alice@test.com"
        )

        whenn()
        val created = suspendTransaction { fixture.sut.createEmployee(employee) }
        val employees = suspendTransaction { fixture.sut.getEmployees(fixture.businessId) }

        then()
        assertEquals(1, employees.size)
        assertEquals("Alice", employees.first().name)
        assertEquals("Smith", employees.first().lastName)
        assertEquals("+1234567890", employees.first().phone)
        assertEquals("alice@test.com", employees.first().email)
        assertEquals(fixture.businessId, created.businessId)
    }

    @Test
    fun `should create employee without phone and email`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = Employee.stub(
            businessId = fixture.businessId,
            name = "Bob",
            lastName = "Jones",
            phone = null,
            email = null
        )

        whenn()
        val created = suspendTransaction { fixture.sut.createEmployee(employee) }
        val found = suspendTransaction { fixture.sut.getEmployee(fixture.businessId, created.id) }

        then()
        assertNotNull(found)
        assertNull(found!!.phone)
        assertNull(found.email)
    }

    @Test
    fun `should create employee linked to user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val employee = Employee.stub(businessId = fixture.businessId, userId = userId)

        whenn()
        val created = suspendTransaction { fixture.sut.createEmployee(employee) }

        then()
        assertEquals(userId, created.userId)
    }

    @Test
    fun `should return empty list when no employees exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val employees = suspendTransaction { fixture.sut.getEmployees(fixture.businessId) }

        then()
        assertTrue(employees.isEmpty())
    }

    @Test
    fun `should return null when employee belongs to another business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getEmployee(Uuid.random(), created.id) }

        then()
        assertNull(found)
    }

    @Test
    fun `should delete employee and return true`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }

        whenn()
        val deleted = suspendTransaction { fixture.sut.deleteEmployee(fixture.businessId, created.id) }
        val remaining = suspendTransaction { fixture.sut.getEmployees(fixture.businessId) }

        then()
        assertTrue(deleted)
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `should return false when deleting non-existent employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()

        whenn()
        val deleted = suspendTransaction { fixture.sut.deleteEmployee(fixture.businessId, Uuid.random()) }

        then()
        assertFalse(deleted)
    }

    @Test
    fun `should update employees linked to user id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val created = suspendTransaction {
            fixture.sut.createEmployee(
                Employee.stub(
                    businessId = fixture.businessId,
                    name = "Old",
                    lastName = "Name",
                    phone = "+1111111111",
                    email = "old@test.com",
                    userId = userId
                )
            )
        }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateIntegratedEmployees(userId, "New", "Surname", "+2222222222", "new@test.com")
        }
        val found = suspendTransaction { fixture.sut.getEmployee(fixture.businessId, created.id) }

        then()
        assertEquals(1, updated)
        assertNotNull(found)
        assertEquals("New", found!!.name)
        assertEquals("Surname", found.lastName)
        assertEquals("+2222222222", found.phone)
        assertEquals("new@test.com", found.email)
        assertEquals(userId, found.userId)
    }

    @Test
    fun `should not update employees belonging to another user when syncing profile`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createEmployee(
                Employee.stub(
                    businessId = fixture.businessId,
                    name = "Keep",
                    lastName = "Me",
                    userId = Uuid.random()
                )
            )
        }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateIntegratedEmployees(Uuid.random(), "New", "Surname", "+2222222222", "new@test.com")
        }
        val found = suspendTransaction { fixture.sut.getEmployee(fixture.businessId, created.id) }

        then()
        assertEquals(0, updated)
        assertEquals("Keep", found!!.name)
    }

    @Test
    fun `should return no services when employee has no assignments`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val created = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getEmployee(fixture.businessId, created.id) }

        then()
        assertTrue(found!!.services.isEmpty())
    }

    @Test
    fun `should expose assigned services on employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService(name = "haircut")
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }

        whenn()
        val found = suspendTransaction { fixture.sut.getEmployee(fixture.businessId, employee.id) }

        then()
        assertEquals(1, found!!.services.size)
        assertEquals(service.id, found.services.first().id)
        assertEquals("haircut", found.services.first().name)
    }

    @Test
    fun `should expose assigned services when listing employees of business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val first = fixture.createService(name = "haircut")
        val second = fixture.createService(name = "shave")
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, first.id) }
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, second.id) }

        whenn()
        val employees = suspendTransaction { fixture.sut.getEmployees(fixture.businessId) }

        then()
        assertEquals(1, employees.size)
        assertEquals(
            setOf(first.id, second.id),
            employees.first().services.map { it.id }.toSet()
        )
    }

    @Test
    fun `should drop service from employee services after unassigning`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService()
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }

        whenn()
        suspendTransaction { fixture.sut.unassignService(employee.id, service.id) }
        val found = suspendTransaction { fixture.sut.getEmployee(fixture.businessId, employee.id) }

        then()
        assertTrue(found!!.services.isEmpty())
    }

    @Test
    fun `should persist services provided on employee creation`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val service = fixture.createService(name = "massage")

        whenn()
        val created = suspendTransaction {
            fixture.sut.createEmployee(
                Employee.stub(businessId = fixture.businessId, services = listOf(service))
            )
        }
        val found = suspendTransaction { fixture.sut.getEmployee(fixture.businessId, created.id) }

        then()
        assertEquals(listOf(service.id), created.services.map { it.id })
        assertEquals(listOf(service.id), found!!.services.map { it.id })
    }

    @Test
    fun `should assign service to employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService()

        whenn()
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }
        val serviceIds = suspendTransaction { fixture.sut.getServiceIds(employee.id) }

        then()
        assertEquals(listOf(service.id), serviceIds)
    }

    @Test
    fun `should not duplicate assignment when assigning same service twice`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService()

        whenn()
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }
        val serviceIds = suspendTransaction { fixture.sut.getServiceIds(employee.id) }

        then()
        assertEquals(1, serviceIds.size)
    }

    @Test
    fun `should unassign service from employee and return true`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService()
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }

        whenn()
        val unassigned = suspendTransaction { fixture.sut.unassignService(employee.id, service.id) }
        val serviceIds = suspendTransaction { fixture.sut.getServiceIds(employee.id) }

        then()
        assertTrue(unassigned)
        assertTrue(serviceIds.isEmpty())
    }

    @Test
    fun `should return false when unassigning service that was never assigned`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService()

        whenn()
        val unassigned = suspendTransaction { fixture.sut.unassignService(employee.id, service.id) }

        then()
        assertFalse(unassigned)
    }

    @Test
    fun `should return employees that can provide a service`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val provider = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId, name = "Provider"))
        }
        suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId, name = "Other"))
        }
        val service = fixture.createService()
        suspendTransaction { fixture.sut.assignService(fixture.businessId, provider.id, service.id) }

        whenn()
        val employees = suspendTransaction { fixture.sut.getEmployeesByService(service.id) }

        then()
        assertEquals(1, employees.size)
        assertEquals("Provider", employees.first().name)
    }

    @Test
    fun `should cascade delete assignments when employee is deleted`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService()
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }

        whenn()
        suspendTransaction { fixture.sut.deleteEmployee(fixture.businessId, employee.id) }
        val employees = suspendTransaction { fixture.sut.getEmployeesByService(service.id) }

        then()
        assertTrue(employees.isEmpty())
    }

    @Test
    fun `should cascade delete assignments when service is deleted`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val employee = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId))
        }
        val service = fixture.createService()
        suspendTransaction { fixture.sut.assignService(fixture.businessId, employee.id, service.id) }

        whenn()
        suspendTransaction { fixture.serviceSut.deleteService(service.id) }
        val serviceIds = suspendTransaction { fixture.sut.getServiceIds(employee.id) }

        then()
        assertTrue(serviceIds.isEmpty())
    }

    @Test
    fun `should return employee by user id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        val created = suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId, userId = userId))
        }

        whenn()
        val found = suspendTransaction { fixture.sut.getEmployeeByUserId(fixture.businessId, userId) }

        then()
        assertNotNull(found)
        assertEquals(created.id, found?.id)
        assertEquals(userId, found?.userId)
    }

    @Test
    fun `should return null when user is not an employee of the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.setup()
        val userId = Uuid.random()
        suspendTransaction {
            fixture.sut.createEmployee(Employee.stub(businessId = fixture.businessId, userId = userId))
        }

        whenn()
        val otherUser = suspendTransaction { fixture.sut.getEmployeeByUserId(fixture.businessId, Uuid.random()) }
        val otherBusiness = suspendTransaction { fixture.sut.getEmployeeByUserId(Uuid.random(), userId) }

        then()
        assertNull(otherUser)
        assertNull(otherBusiness)
    }
}
