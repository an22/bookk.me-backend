package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.operation.UpdateEmployee
import com.bookk.business.domain.datasource.BusinessDataSource
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import library.permissions.ObjectPermission
import library.schedule.DayOffRange
import library.schedule.Schedule
import library.schedule.WorkHour
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class UpdateEmployeeImplTest {

    private val requestUserId = Uuid.random()

    private class SutFixture {
        val employeeDataSource = mockk<EmployeeDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateEmployeeImpl(employeeDataSource, businessDataSource, transactionManager)

        init {
            coEvery { businessDataSource.getPermission(any(), any()) } returns ObjectPermission.EDIT.int
        }

        fun grantPermission(permission: ObjectPermission?) {
            coEvery { businessDataSource.getPermission(any(), any()) } returns permission?.int
        }
    }

    @Test
    fun `should update employee successfully`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub()
        coEvery { fixture.employeeDataSource.updateEmployee(employee) } returns employee

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.isSuccess)
        assertEquals(employee, result.getOrNull())
        coVerify(exactly = 1) { fixture.employeeDataSource.updateEmployee(employee) }
    }

    @Test
    fun `should update employee without phone or email`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub(phone = null, email = null)
        coEvery { fixture.employeeDataSource.updateEmployee(employee) } returns employee

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.employeeDataSource.updateEmployee(employee) }
    }

    @Test
    fun `should return failure when name is invalid`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub(name = "a".repeat(600))

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is UpdateEmployee.Error.ValidationError)
        coVerify(exactly = 0) { fixture.employeeDataSource.updateEmployee(any()) }
    }

    @Test
    fun `should return failure when last name is invalid`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub(lastName = "a".repeat(600))

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is UpdateEmployee.Error.ValidationError)
        coVerify(exactly = 0) { fixture.employeeDataSource.updateEmployee(any()) }
    }

    @Test
    fun `should return failure when phone is invalid`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub(phone = "not-a-phone")

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is UpdateEmployee.Error.ValidationError)
        coVerify(exactly = 0) { fixture.employeeDataSource.updateEmployee(any()) }
    }

    @Test
    fun `should return failure when email is invalid`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val employee = Employee.stub(email = "not-an-email")

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is UpdateEmployee.Error.ValidationError)
        coVerify(exactly = 0) { fixture.employeeDataSource.updateEmployee(any()) }
    }

    @Test
    fun `should return failure when active day has no work hours`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val schedule = Schedule(workingDays = listOf(DayOfWeek.MONDAY), workingHours = emptyMap())
        val employee = Employee.stub(schedule = schedule)

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is UpdateEmployee.Error.ActiveDayWithoutWorkHours)
        coVerify(exactly = 0) { fixture.employeeDataSource.updateEmployee(any()) }
    }

    @Test
    fun `should return failure when day off range start date is after end date`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val dayOffs = listOf(DayOffRange(LocalDate(2099, 12, 31), LocalDate(2099, 12, 30)))
        val schedule = Schedule(workingDays = emptyList(), workingHours = emptyMap(), dayOffs = dayOffs)
        val employee = Employee.stub(schedule = schedule)

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is UpdateEmployee.Error.InvalidDayOffRange)
        coVerify(exactly = 0) { fixture.employeeDataSource.updateEmployee(any()) }
    }

    @Test
    fun `should return success when an active day has work hours`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val schedule = Schedule(
            workingDays = listOf(DayOfWeek.MONDAY),
            workingHours = mapOf(DayOfWeek.MONDAY to listOf(WorkHour(LocalTime(9, 0), LocalTime(17, 0))))
        )
        val employee = Employee.stub(schedule = schedule)
        coEvery { fixture.employeeDataSource.updateEmployee(employee) } returns employee

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when user has no edit permission for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        fixture.grantPermission(ObjectPermission.READ)
        val employee = Employee.stub()

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.employeeDataSource.updateEmployee(any()) }
    }

    @Test
    fun `should return failure when user has no permission record for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        fixture.grantPermission(null)
        val employee = Employee.stub()

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should assert edit permission against the employee business id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val employee = Employee.stub(businessId = businessId)
        coEvery { fixture.employeeDataSource.updateEmployee(employee) } returns employee

        whenn()
        val result = fixture.sut(requestUserId, employee)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.businessDataSource.getPermission(requestUserId, businessId) }
    }
}
