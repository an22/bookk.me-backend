package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation
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

internal class CreateEmployeeInvitationImplTest {

    private class SutFixture {
        val invitationDataSource = mockk<EmployeeInvitationDataSource>()
        val employeeDataSource = mockk<EmployeeDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val userClient = mockk<UserClient>()
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val sut = CreateEmployeeInvitationImpl(
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

    @Test
    fun `should create invitation successfully`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        val stored = invitation.copy(invitedBy = requestUserId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.OWNER.int
            coEvery {
                employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email)
            } returns null
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns
                business(invitation.businessId)
            coEvery { invitationDataSource.createInvitation(stored) } returns stored
            coEvery { userClient.getUserByEmail(stored.email) } returns Result.failure(RuntimeException("not found"))
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isSuccess)
        assertEquals(stored, result.getOrNull())
        coVerify(exactly = 1) { fixture.invitationDataSource.createInvitation(stored) }
    }

    @Test
    fun `should store the authenticated caller as the inviter`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(invitedBy = Uuid.random())
        val persisted = slot<EmployeeInvitation>()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.OWNER.int
            coEvery {
                employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email)
            } returns null
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns
                business(invitation.businessId)
            coEvery { invitationDataSource.createInvitation(capture(persisted)) } answers { persisted.captured }
            coEvery { userClient.getUserByEmail(invitation.email) } returns Result.failure(RuntimeException("not found"))
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isSuccess)
        assertEquals(requestUserId, persisted.captured.invitedBy)
    }

    @Test
    fun `should publish invitation created event when invited email belongs to a registered user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(email = "alice@test.com")
        val stored = invitation.copy(invitedBy = requestUserId)
        val invitedUserId = Uuid.random()
        val event = slot<BusinessEvent.EmployeeInvitationCreated>()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.OWNER.int
            coEvery {
                employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email)
            } returns null
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns
                business(invitation.businessId, name = "Barbershop")
            coEvery { invitationDataSource.createInvitation(stored) } returns stored
            coEvery { userClient.getUserByEmail(stored.email) } returns
                Result.success(UserSnapshot.stub(id = invitedUserId, email = stored.email))
            coEvery { eventProducer.send(capture(event), any()) } returns Unit
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(any(BusinessEvent.EmployeeInvitationCreated::class), any())
        }
        assertEquals(invitedUserId, event.captured.invitedUserId)
        assertEquals(invitation.businessId, event.captured.businessId)
        assertEquals("Barbershop", event.captured.businessName)
    }

    @Test
    fun `should not publish an event when the invited email has no registered account`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(email = "unregistered@test.com")
        val stored = invitation.copy(invitedBy = requestUserId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.OWNER.int
            coEvery {
                employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email)
            } returns null
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns
                business(invitation.businessId)
            coEvery { invitationDataSource.createInvitation(stored) } returns stored
            coEvery { userClient.getUserByEmail(stored.email) } returns Result.failure(RuntimeException("not found"))
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }

    @Test
    fun `should return failure when email is blank`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub(email = " ")

        whenn()
        val result = fixture.sut(Uuid.random(), invitation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateEmployeeInvitation.Error.ValidationError)
    }

    @Test
    fun `should return failure when email is invalid`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub(email = "not-an-email")

        whenn()
        val result = fixture.sut(Uuid.random(), invitation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateEmployeeInvitation.Error.ValidationError)
    }

    @Test
    fun `should return failure when email is too long`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitation = EmployeeInvitation.stub(email = "${"a".repeat(513)}@test.com")

        whenn()
        val result = fixture.sut(Uuid.random(), invitation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateEmployeeInvitation.Error.ValidationError)
    }

    @Test
    fun `should return failure when permission denied`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.READ.int
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
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
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.OWNER.int
            coEvery {
                employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email)
            } returns Employee.stub(businessId = invitation.businessId, email = invitation.email)
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateEmployeeInvitation.Error.EmployeeExist)
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
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.OWNER.int
            coEvery {
                employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email)
            } returns null
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns null
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }

    @Test
    fun `should return failure when invitation already exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub()
        val stored = invitation.copy(invitedBy = requestUserId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                businessDataSource.getPermission(requestUserId, invitation.businessId)
            } returns ObjectPermission.OWNER.int
            coEvery {
                employeeDataSource.getEmployeeByEmail(invitation.businessId, invitation.email)
            } returns null
            coEvery { businessDataSource.getBusinessById(invitation.businessId) } returns
                business(invitation.businessId)
            coEvery {
                invitationDataSource.createInvitation(stored)
            } throws Error.UniqueConstraintFailed("", RuntimeException())
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateEmployeeInvitation.Error.InvitationExist)
        coVerify(exactly = 0) { fixture.eventProducer.send(any(), any()) }
    }
}
