package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ResourcePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class GetEmployeePermissionsImplTest {

    private val requestUserId = Uuid.random()
    private val businessId = Uuid.random()

    private class SutFixture {
        val employeeDataSource = mockk<EmployeeDataSource>()
        val businessPermissionDataSource = mockk<BusinessPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetEmployeePermissionsImpl(employeeDataSource, businessPermissionDataSource, transactionManager)

        init {
            coEvery { businessPermissionDataSource.getPermission(any(), any(), BusinessResource.EMPLOYEES) } returns ResourcePermission(view = true)
        }
    }

    @Test
    fun `should return the employee's current permissions`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employee = Employee.stub(businessId = businessId)
        val permissions = BusinessPermissions.stub(clients = ResourcePermission.FULL)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { employeeDataSource.getEmployee(businessId, employee.id) } returns employee
            coEvery { businessPermissionDataSource.getPermissions(employee.userId, businessId) } returns permissions
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, employee.id)

        then()
        assertTrue(result.isSuccess)
        assertEquals(permissions, result.getOrNull())
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
        val result = fixture.sut(requestUserId, businessId, employeeId)

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }

    @Test
    fun `should return failure when caller cannot view employees`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val employeeId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessPermissionDataSource.getPermission(requestUserId, businessId, BusinessResource.EMPLOYEES) } returns ResourcePermission.NONE
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, employeeId)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.employeeDataSource.getEmployee(any(), any()) }
    }
}
