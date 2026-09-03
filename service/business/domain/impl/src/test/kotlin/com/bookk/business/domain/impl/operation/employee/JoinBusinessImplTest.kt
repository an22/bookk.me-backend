package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.JoinBusiness
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
import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.api.UserSnapshot
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

internal class JoinBusinessImplTest {

    private class SutFixture {
        val invitationDataSource = mockk<EmployeeInvitationDataSource>()
        val employeeDataSource = mockk<EmployeeDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val userClient = mockk<UserClient>()
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val sut = JoinBusinessImpl(
            invitationDataSource,
            employeeDataSource,
            businessDataSource,
            userClient,
            transactionManager,
            eventProducer
        )
    }

    private fun business(id: Uuid, name: String = "Barbershop") = Business.stub(
        id = id,
        name = name,
        description = "",
        address = "1 Main St",
        timeZone = TimeZone.UTC,
        currencyCode = "USD"
    )

    private fun userSnapshot(
        id: Uuid,
        name: String = "Alice",
        lastName: String = "Smith",
        phone: String? = null
    ) = UserSnapshot.stub(id = id, name = name, lastName = lastName, phone = phone)

    private fun SutFixture.stubHappyPath(requestUserId: Uuid, invitation: EmployeeInvitation, employee: Employee) {
        transactionManager.mockTransaction()
        coEvery { invitationDataSource.getInvitationByCode(requireNotNull(invitation.code)) } returns invitation
        coEvery { employeeDataSource.getEmployeeByUserId(invitation.businessId, requestUserId) } returns null
        coEvery { invitationDataSource.redeemInvitation(invitation.id) } returns true
        coEvery { userClient.getUserById(requestUserId) } returns Result.success(userSnapshot(requestUserId))
        coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns business(invitation.businessId)
        coEvery { employeeDataSource.createEmployee(any()) } returns employee
        coEvery { businessDataSource.setUserPermissions(requestUserId, invitation.businessId, any()) } returns Unit
    }

    @Test
    fun `should create employee from invitation when redeemed with a valid code`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        val employee = Employee.stub(businessId = invitation.businessId, userId = requestUserId)
        val createdEmployee = slot<Employee>()
        with(fixture) {
            stubHappyPath(requestUserId, invitation, employee)
            coEvery { userClient.getUserById(requestUserId) } returns
                Result.success(userSnapshot(requestUserId, name = "Alice", lastName = "Smith", phone = "+10000000000"))
            coEvery { employeeDataSource.createEmployee(capture(createdEmployee)) } returns employee
        }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isSuccess)
        assertEquals(employee, result.getOrNull())
        assertEquals("Alice", createdEmployee.captured.name)
        assertEquals("Smith", createdEmployee.captured.lastName)
        assertEquals("+10000000000", createdEmployee.captured.phone)
        assertEquals(requestUserId, createdEmployee.captured.userId)
        assertEquals(invitation.businessId, createdEmployee.captured.businessId)
        coVerify(exactly = 1) { fixture.invitationDataSource.redeemInvitation(invitation.id) }
    }

    @Test
    fun `should grant read permission to the new employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        val employee = Employee.stub(businessId = invitation.businessId, userId = requestUserId)
        with(fixture) { stubHappyPath(requestUserId, invitation, employee) }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.setUserPermissions(
                requestUserId,
                invitation.businessId,
                ObjectPermission.READ.int
            )
        }
    }

    @Test
    fun `should publish invitation redeemed event addressed to the inviter`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val inviterUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(invitedBy = inviterUserId)
        val employee = Employee.stub(
            businessId = invitation.businessId,
            userId = requestUserId,
            name = "Alice"
        )
        val event = slot<BusinessEvent.EmployeeInvitationRedeemed>()
        with(fixture) {
            stubHappyPath(requestUserId, invitation, employee)
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns
                business(invitation.businessId, name = "Barbershop")
            coEvery {
                eventProducer.send(any(BusinessEvent.EmployeeInvitationRedeemed::class), any())
            } answers { event.captured = firstArg() }
        }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(any(BusinessEvent.EmployeeInvitationRedeemed::class), any())
        }
        assertEquals(inviterUserId, event.captured.inviterUserId)
        assertEquals(requestUserId, event.captured.employeeUserId)
        assertEquals("Alice", event.captured.employeeName)
        assertEquals(invitation.businessId, event.captured.businessId)
        assertEquals("Barbershop", event.captured.businessName)
    }

    @Test
    fun `should publish permission changed event granting read to the appointments service`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        val employee = Employee.stub(businessId = invitation.businessId, userId = requestUserId)
        val event = slot<BusinessEvent.EmployeePermissionChanged>()
        with(fixture) {
            stubHappyPath(requestUserId, invitation, employee)
            coEvery {
                eventProducer.send(any(BusinessEvent.EmployeePermissionChanged::class), any())
            } answers { event.captured = firstArg() }
        }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(any(BusinessEvent.EmployeePermissionChanged::class), any())
        }
        assertEquals(requestUserId, event.captured.employeeUserId)
        assertEquals(invitation.businessId, event.captured.businessId)
        assertEquals(ObjectPermission.READ.int, event.captured.permission)
    }

    @Test
    fun `should return failure when invite code is unknown`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val code = "UNKNOWN1"
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitationByCode(code) } returns null
        }

        whenn()
        val result = fixture.sut(Uuid.random(), code)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.NotFound)
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when invitation is already redeemed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(status = EmployeeInvitationStatus.REDEEMED)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitationByCode(requireNotNull(invitation.code)) } returns invitation
        }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is JoinBusiness.Error.InvitationAlreadyProcessed)
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when invitation was concurrently redeemed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitationByCode(requireNotNull(invitation.code)) } returns invitation
            coEvery { employeeDataSource.getEmployeeByUserId(invitation.businessId, requestUserId) } returns null
            coEvery { invitationDataSource.redeemInvitation(invitation.id) } returns false
        }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is JoinBusiness.Error.InvitationAlreadyProcessed)
        coVerify(exactly = 0) { fixture.employeeDataSource.createEmployee(any()) }
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when user is already an employee`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitationByCode(requireNotNull(invitation.code)) } returns invitation
            coEvery {
                employeeDataSource.getEmployeeByUserId(invitation.businessId, requestUserId)
            } returns Employee.stub(businessId = invitation.businessId, userId = requestUserId)
        }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is JoinBusiness.Error.EmployeeExist)
        coVerify(exactly = 0) { fixture.employeeDataSource.createEmployee(any()) }
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when business does not exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitationByCode(requireNotNull(invitation.code)) } returns invitation
            coEvery { employeeDataSource.getEmployeeByUserId(invitation.businessId, requestUserId) } returns null
            coEvery { invitationDataSource.redeemInvitation(invitation.id) } returns true
            coEvery { userClient.getUserById(requestUserId) } returns Result.success(userSnapshot(requestUserId))
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns null
        }

        whenn()
        val result = fixture.sut(requestUserId, requireNotNull(invitation.code))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.NotFound)
        coVerify(exactly = 0) { fixture.employeeDataSource.createEmployee(any()) }
    }
}
