package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.operation.SetEmployeePermission
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.business.client.api.event.BusinessEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ResourcePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class SetEmployeePermissionImplTest {

    private val requestUserId = Uuid.random()
    private val businessId = Uuid.random()

    private class SutFixture {
        val employeeDataSource = mockk<EmployeeDataSource>()
        val businessPermissionDataSource = mockk<BusinessPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val sut = SetEmployeePermissionImpl(employeeDataSource, businessPermissionDataSource, transactionManager, eventProducer)

        init {
            coEvery { businessPermissionDataSource.getPermission(any(), any(), BusinessResource.EMPLOYEES) } returns ResourcePermission(update = true)
        }
    }

    @Test
    fun `should grant the requested permission and return the employee's updated permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = Employee.stub(businessId = businessId)
        val updated = BusinessPermissions.stub(clients = ResourcePermission.FULL)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employee.id) } returns employee
            coEvery { businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.CLIENTS) } returns ResourcePermission.FULL
            coEvery { businessPermissionDataSource.setPermission(employee.userId, businessId, BusinessResource.CLIENTS, ResourcePermission.FULL) } returns Unit
            coEvery { businessPermissionDataSource.getPermissions(employee.userId, businessId) } returns updated
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, employee.id, BusinessResource.CLIENTS, ResourcePermission.FULL)

        then()
        assertTrue(result.isSuccess)
        assertEquals(updated, result.getOrNull())
        coVerify(exactly = 1) {
            fixture.businessPermissionDataSource.setPermission(employee.userId, businessId, BusinessResource.CLIENTS, ResourcePermission.FULL)
        }
    }

    @Test
    fun `should publish an employee permissions changed event`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = Employee.stub(businessId = businessId)
        val updated = BusinessPermissions.stub(clients = ResourcePermission(view = true))
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employee.id) } returns employee
            coEvery { businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.CLIENTS) } returns ResourcePermission.FULL
            coEvery { businessPermissionDataSource.setPermission(any(), any(), any(), any()) } returns Unit
            coEvery { businessPermissionDataSource.getPermissions(employee.userId, businessId) } returns updated
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, employee.id, BusinessResource.CLIENTS, ResourcePermission(view = true))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(
                match<BusinessEvent.EmployeePermissionsChanged> {
                    it.employeeUserId == employee.userId && it.businessId == businessId && it.permissions == updated
                },
                any()
            )
        }
    }

    @Test
    fun `should return failure when employee does not exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employeeId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employeeId) } returns null
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, employeeId, BusinessResource.CLIENTS, ResourcePermission.FULL)

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }

    @Test
    fun `should return failure when the caller cannot grant a permission level they do not hold`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = Employee.stub(businessId = businessId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employee.id) } returns employee
            coEvery { businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.CLIENTS) } returns ResourcePermission(view = true)
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, employee.id, BusinessResource.CLIENTS, ResourcePermission.FULL)

        then()
        assertTrue(result.exceptionOrNull() is SetEmployeePermission.Error.InsufficientGrant)
        coVerify(exactly = 0) { fixture.businessPermissionDataSource.setPermission(any(), any(), any(), any()) }
    }

    @Test
    fun `should return failure when caller cannot update employees`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = Employee.stub(businessId = businessId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.EMPLOYEES) } returns ResourcePermission(view = true)
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, employee.id, BusinessResource.CLIENTS, ResourcePermission.FULL)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.employeeDataSource.getEmployee(any(), any()) }
    }
}
