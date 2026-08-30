package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeRole
import com.bookk.business.domain.datasource.BusinessDataSource
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
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class PromoteEmployeeImplTest {

    private val requestUserId = Uuid.random()
    private val businessId = Uuid.random()

    private class SutFixture {
        val employeeDataSource = mockk<EmployeeDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val sut = PromoteEmployeeImpl(employeeDataSource, businessDataSource, transactionManager, eventProducer)

        init {
            coEvery { businessDataSource.getPermission(any(), any()) } returns ObjectPermission.OWNER.int
            coEvery { businessDataSource.setUserPermissions(any(), any(), any()) } returns Unit
        }

        fun grantPermission(permission: ObjectPermission?) {
            coEvery { businessDataSource.getPermission(any(), any()) } returns permission?.int
        }
    }

    @Test
    fun `should promote employee to manager successfully`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub(businessId = businessId)
        coEvery { fixture.employeeDataSource.getEmployee(businessId, employee.id) } returns employee

        whenn()
        val result = fixture.sut(requestUserId, businessId, employee.id, EmployeeRole.MANAGER)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.setUserPermissions(employee.userId, businessId, ObjectPermission.EDIT.int)
        }
        coVerify(exactly = 1) {
            fixture.eventProducer.send(
                match<BusinessEvent.EmployeePermissionChanged> {
                    it.employeeUserId == employee.userId && it.businessId == businessId && it.permission == ObjectPermission.EDIT.int
                },
                any()
            )
        }
    }

    @Test
    fun `should promote employee to employee level successfully`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub(businessId = businessId)
        coEvery { fixture.employeeDataSource.getEmployee(businessId, employee.id) } returns employee

        whenn()
        val result = fixture.sut(requestUserId, businessId, employee.id, EmployeeRole.EMPLOYEE)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.setUserPermissions(employee.userId, businessId, ObjectPermission.READ.int)
        }
        coVerify(exactly = 1) {
            fixture.eventProducer.send(
                match<BusinessEvent.EmployeePermissionChanged> {
                    it.employeeUserId == employee.userId && it.businessId == businessId && it.permission == ObjectPermission.READ.int
                },
                any()
            )
        }
    }

    @Test
    fun `should return failure when employee does not exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employeeId = Uuid.random()
        coEvery { fixture.employeeDataSource.getEmployee(businessId, employeeId) } returns null

        whenn()
        val result = fixture.sut(requestUserId, businessId, employeeId, EmployeeRole.MANAGER)

        then()
        assertTrue(result.exceptionOrNull() is Error.NotFound)
        coVerify(exactly = 0) { fixture.businessDataSource.setUserPermissions(any(), any(), any()) }
    }

    @Test
    fun `should return failure when caller has no owner permission for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        fixture.grantPermission(ObjectPermission.EDIT)
        val employeeId = Uuid.random()

        whenn()
        val result = fixture.sut(requestUserId, businessId, employeeId, EmployeeRole.MANAGER)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.employeeDataSource.getEmployee(any(), any()) }
        coVerify(exactly = 0) { fixture.businessDataSource.setUserPermissions(any(), any(), any()) }
    }

    @Test
    fun `should return failure when caller has no permission record for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        fixture.grantPermission(null)
        val employeeId = Uuid.random()

        whenn()
        val result = fixture.sut(requestUserId, businessId, employeeId, EmployeeRole.MANAGER)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }
}
