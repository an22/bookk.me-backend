package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.RejectEmployeeInvitation
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.api.UserSnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class RejectEmployeeInvitationImplTest {

    private class SutFixture {
        val invitationDataSource = mockk<EmployeeInvitationDataSource>()
        val userClient = mockk<UserClient>()
        val transactionManager = mockk<TransactionManager>()
        val sut = RejectEmployeeInvitationImpl(invitationDataSource, userClient, transactionManager)
    }

    private fun userSnapshot(id: Uuid, email: String) =
        UserSnapshot.stub(id = id, email = email)

    @Test
    fun `should reject invitation addressed to the calling user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(email = "alice@test.com")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
            coEvery { userClient.getUserById(requestUserId) } returns
                Result.success(userSnapshot(requestUserId, invitation.email))
            coEvery { invitationDataSource.rejectInvitation(invitation.id) } returns true
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.invitationDataSource.rejectInvitation(invitation.id) }
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
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }

    @Test
    fun `should return failure when invitation is addressed to another user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(email = "alice@test.com")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
            coEvery { userClient.getUserById(requestUserId) } returns
                Result.success(userSnapshot(requestUserId, "someone-else@test.com"))
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.invitationDataSource.rejectInvitation(any()) }
    }

    @Test
    fun `should return failure when invitation is already approved`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(email = "alice@test.com", status = EmployeeInvitationStatus.APPROVED)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
            coEvery { userClient.getUserById(requestUserId) } returns
                Result.success(userSnapshot(requestUserId, invitation.email))
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.exceptionOrNull() is RejectEmployeeInvitation.Error.InvitationAlreadyProcessed)
        coVerify(exactly = 0) { fixture.invitationDataSource.rejectInvitation(any()) }
    }

    @Test
    fun `should return failure when invitation was concurrently processed`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val invitation = EmployeeInvitation.stub(email = "alice@test.com")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getInvitation(invitation.businessId, invitation.id) } returns invitation
            coEvery { userClient.getUserById(requestUserId) } returns
                Result.success(userSnapshot(requestUserId, invitation.email))
            coEvery { invitationDataSource.rejectInvitation(invitation.id) } returns false
        }

        whenn()
        val result = fixture.sut(requestUserId, invitation.businessId, invitation.id)

        then()
        assertTrue(result.exceptionOrNull() is RejectEmployeeInvitation.Error.InvitationAlreadyProcessed)
    }
}
