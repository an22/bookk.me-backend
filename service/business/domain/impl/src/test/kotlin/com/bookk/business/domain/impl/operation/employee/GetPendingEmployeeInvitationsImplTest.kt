package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.api.UserSnapshot
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class GetPendingEmployeeInvitationsImplTest {

    private class SutFixture {
        val invitationDataSource = mockk<EmployeeInvitationDataSource>()
        val userClient = mockk<UserClient>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetPendingEmployeeInvitationsImpl(invitationDataSource, userClient, transactionManager)
    }

    @Test
    fun `should return pending invitations addressed to the caller's email`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val invitations = listOf(EmployeeInvitation.stub(businessId = businessId, email = "alice@test.com"))
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { userClient.getUserById(userId) } returns
                Result.success(UserSnapshot.stub(id = userId, email = "alice@test.com"))
            coEvery { invitationDataSource.getPendingInvitations(businessId, "alice@test.com") } returns invitations
        }

        whenn()
        val result = fixture.sut(userId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(invitations, result.getOrNull())
    }

    @Test
    fun `should return empty list when caller has no pending invitations`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { userClient.getUserById(userId) } returns
                Result.success(UserSnapshot.stub(id = userId, email = "alice@test.com"))
            coEvery { invitationDataSource.getPendingInvitations(businessId, "alice@test.com") } returns emptyList()
        }

        whenn()
        val result = fixture.sut(userId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `should return failure when caller cannot be resolved`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { userClient.getUserById(userId) } returns Result.failure(RuntimeException("not found"))
        }

        whenn()
        val result = fixture.sut(userId, businessId)

        then()
        assertTrue(result.isFailure)
    }
}
