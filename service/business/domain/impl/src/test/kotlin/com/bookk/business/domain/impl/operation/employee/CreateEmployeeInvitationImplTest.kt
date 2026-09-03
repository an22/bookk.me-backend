package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
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
import io.mockk.slot
import kotlinx.datetime.TimeZone
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class CreateEmployeeInvitationImplTest {

    private class SutFixture {
        val invitationDataSource = mockk<EmployeeInvitationDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = CreateEmployeeInvitationImpl(invitationDataSource, businessDataSource, transactionManager)
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
        val businessId = Uuid.random()
        val persisted = slot<EmployeeInvitation>()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(requestUserId, businessId) } returns ObjectPermission.OWNER.int
            coEvery { businessDataSource.getBusinessById(businessId) } returns business(businessId)
            coEvery { invitationDataSource.createInvitation(capture(persisted)) } answers { persisted.captured }
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(requestUserId, persisted.captured.invitedBy)
        assertEquals(businessId, persisted.captured.businessId)
        assertEquals(EmployeeInvitationStatus.PENDING, persisted.captured.status)
        assertNotNull(persisted.captured.code)
        coVerify(exactly = 1) { fixture.invitationDataSource.createInvitation(any()) }
    }

    @Test
    fun `should retry with a new code when the generated code collides`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val businessId = Uuid.random()
        val invitations = mutableListOf<EmployeeInvitation>()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(requestUserId, businessId) } returns ObjectPermission.OWNER.int
            coEvery { businessDataSource.getBusinessById(businessId) } returns business(businessId)
            coEvery { invitationDataSource.createInvitation(any()) } answers {
                val invitation = firstArg<EmployeeInvitation>()
                invitations.add(invitation)
                if (invitations.size == 1) throw Error.UniqueConstraintFailed("", RuntimeException())
                invitation
            }
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(2, invitations.size)
        assertTrue(invitations[0].code != invitations[1].code)
    }

    @Test
    fun `should return failure when code generation keeps colliding`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(requestUserId, businessId) } returns ObjectPermission.OWNER.int
            coEvery { businessDataSource.getBusinessById(businessId) } returns business(businessId)
            coEvery {
                invitationDataSource.createInvitation(any())
            } throws Error.UniqueConstraintFailed("", RuntimeException())
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.UniqueConstraintFailed)
    }

    @Test
    fun `should return failure when permission denied`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(requestUserId, businessId) } returns ObjectPermission.READ.int
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.invitationDataSource.createInvitation(any()) }
    }

    @Test
    fun `should return failure when business does not exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val requestUserId = Uuid.random()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(requestUserId, businessId) } returns ObjectPermission.OWNER.int
            coEvery { businessDataSource.getBusinessById(businessId) } returns null
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.NotFound)
    }
}
