package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.operation.GetPendingEmployeeInvitationsByEmail.Error
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GetPendingEmployeeInvitationsByEmailImplTest {

    private class SutFixture {
        val invitationDataSource = mockk<EmployeeInvitationDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetPendingEmployeeInvitationsByEmailImpl(invitationDataSource, transactionManager)
    }

    @Test
    fun `should return pending invitations for the given email`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val invitations = listOf(EmployeeInvitation.stub(email = "alice@test.com"))
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getPendingInvitationsByEmail("alice@test.com") } returns invitations
        }

        whenn()
        val result = fixture.sut("alice@test.com")

        then()
        assertTrue(result.isSuccess)
        assertEquals(invitations, result.getOrNull())
    }

    @Test
    fun `should return empty list when email has no pending invitations`() = runUnitTest {
        given()
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { invitationDataSource.getPendingInvitationsByEmail("alice@test.com") } returns emptyList()
        }

        whenn()
        val result = fixture.sut("alice@test.com")

        then()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `should return failure when email is blank`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val result = fixture.sut("")

        then()
        assertTrue(result.exceptionOrNull() is Error.ValidationError)
    }

    @Test
    fun `should return failure when email is not a valid format`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val result = fixture.sut("not-an-email")

        then()
        assertTrue(result.exceptionOrNull() is Error.ValidationError)
    }
}
