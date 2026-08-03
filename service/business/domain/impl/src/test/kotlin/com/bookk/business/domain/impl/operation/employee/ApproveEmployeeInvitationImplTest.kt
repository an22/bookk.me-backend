package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.ApproveEmployeeInvitation
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
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
import io.mockk.slot
import kotlinx.datetime.TimeZone
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class ApproveEmployeeInvitationImplTest {

    private class SutFixture {
        val invitationDataSource = mockk<EmployeeInvitationDataSource>()
        val employeeDataSource = mockk<EmployeeDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val sut = ApproveEmployeeInvitationImpl(
            invitationDataSource,
            employeeDataSource,
            businessDataSource,
            transactionManager,
            eventProducer
        )
    }

    private fun business(id: Uuid, name: String = "Barbershop") = Business(
        id = id,
        name = name,
        description = "",
        address = "1 Main St",
        timeZone = TimeZone.UTC,
        location = null,
        currencyCode = "USD",
        socials = emptyList()
    )

    private fun SutFixture.stubHappyPath(invitation: EmployeeInvitation, employee: Employee) {
        transactionManager.mockTransaction()
        coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
        coEvery { employeeDataSource.getEmployeeByUserId(invitation.businessId, invitation.userId) } returns null
        coEvery { invitationDataSource.approveInvitation(invitation.id) } returns true
        coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns business(invitation.businessId)
        coEvery { employeeDataSource.createEmployee(any()) } returns employee
        coEvery { businessDataSource.setUserPermissions(invitation.userId, invitation.businessId, any()) } returns Unit
    }

    @Test
    fun `should create employee from invitation when approved by invited user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub(name = "Alice", lastName = "Smith")
        val employee = Employee.stub(businessId = invitation.businessId, userId = invitation.userId)
        val createdEmployee = slot<Employee>()
        with(fixture) {
            stubHappyPath(invitation, employee)
            coEvery { employeeDataSource.createEmployee(capture(createdEmployee)) } returns employee
        }

        whenn()
        val result = fixture.sut(invitation.userId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isSuccess)
        assertEquals(employee, result.getOrNull())
        assertEquals("Alice", createdEmployee.captured.name)
        assertEquals("Smith", createdEmployee.captured.lastName)
        assertEquals(invitation.userId, createdEmployee.captured.userId)
        assertEquals(invitation.businessId, createdEmployee.captured.businessId)
        coVerify(exactly = 1) { fixture.invitationDataSource.approveInvitation(invitation.id) }
    }

    @Test
    fun `should grant read permission to the approved employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub()
        val employee = Employee.stub(businessId = invitation.businessId, userId = invitation.userId)
        with(fixture) { stubHappyPath(invitation, employee) }

        whenn()
        val result = fixture.sut(invitation.userId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.setUserPermissions(
                invitation.userId,
                invitation.businessId,
                ObjectPermission.READ.int
            )
        }
    }

    @Test
    fun `should publish invitation approved event addressed to the inviter`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val inviterUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(invitedBy = inviterUserId)
        val employee = Employee.stub(
            businessId = invitation.businessId,
            userId = invitation.userId,
            name = "Alice"
        )
        val event = slot<BusinessEvent.EmployeeInvitationApproved>()
        with(fixture) {
            stubHappyPath(invitation, employee)
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns
                business(invitation.businessId, name = "Barbershop")
            coEvery { eventProducer.send(capture(event), any()) } returns Unit
        }

        whenn()
        val result = fixture.sut(invitation.userId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(any(BusinessEvent.EmployeeInvitationApproved::class), any())
        }
        assertEquals(inviterUserId, event.captured.inviterUserId)
        assertEquals(invitation.userId, event.captured.employeeUserId)
        assertEquals("Alice", event.captured.employeeName)
        assertEquals(invitation.businessId, event.captured.businessId)
        assertEquals("Barbershop", event.captured.businessName)
    }

    @Test
    fun `should return failure when invitation does not exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val id = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(businessId, id) } returns null
        }

        whenn()
        val result = fixture.sut(Uuid.random(), businessId, id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.NotFound)
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when invitation is approved by another user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
        }

        whenn()
        val result = fixture.sut(Uuid.random(), invitation.businessId, invitation.id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when invitation is already approved`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub(status = EmployeeInvitationStatus.APPROVED)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
        }

        whenn()
        val result = fixture.sut(invitation.userId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApproveEmployeeInvitation.Error.InvitationAlreadyProcessed)
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when invitation was concurrently approved`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
            coEvery {
                employeeDataSource.getEmployeeByUserId(invitation.businessId, invitation.userId)
            } returns null
            coEvery { invitationDataSource.approveInvitation(invitation.id) } returns false
        }

        whenn()
        val result = fixture.sut(invitation.userId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApproveEmployeeInvitation.Error.InvitationAlreadyProcessed)
        coVerify(exactly = 0) { fixture.employeeDataSource.createEmployee(any()) }
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when user is already an employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
            coEvery {
                employeeDataSource.getEmployeeByUserId(invitation.businessId, invitation.userId)
            } returns Employee.stub(businessId = invitation.businessId, userId = invitation.userId)
        }

        whenn()
        val result = fixture.sut(invitation.userId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApproveEmployeeInvitation.Error.EmployeeExist)
        coVerify(exactly = 0) { fixture.employeeDataSource.createEmployee(any()) }
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when business does not exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
            coEvery {
                employeeDataSource.getEmployeeByUserId(invitation.businessId, invitation.userId)
            } returns null
            coEvery { invitationDataSource.approveInvitation(invitation.id) } returns true
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns null
        }

        whenn()
        val result = fixture.sut(invitation.userId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.NotFound)
        coVerify(exactly = 0) { fixture.employeeDataSource.createEmployee(any()) }
    }
}
