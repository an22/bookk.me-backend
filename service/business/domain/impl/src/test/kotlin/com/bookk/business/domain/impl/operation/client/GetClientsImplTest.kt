package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.api.client.entity.toRemote
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ClientDataSource
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
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class GetClientsImplTest {

    private val requestUserId = Uuid.random()

    private class SutFixture {
        val clientDataSource = mockk<ClientDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetClientsImpl(clientDataSource, businessDataSource, transactionManager)

        init {
            coEvery { businessDataSource.getPermission(any(), any()) } returns ObjectPermission.OWNER.int
        }

        fun grantPermission(permission: ObjectPermission?) {
            coEvery { businessDataSource.getPermission(any(), any()) } returns permission?.int
        }
    }

    @Test
    fun `should return clients list when clients exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val clients = listOf(
            Client.Detached.stub(),
            Client.Integrated.stub()
        )
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.getClients(businessId) } returns clients
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isSuccess)
        val actual = result.getOrNull()
        val expected = clients.map { it.toRemote() }
        assertEquals(expected.size, actual?.size)
        expected.forEachIndexed { index, exp ->
            val act = actual!![index]
            assertEquals(exp.id, act.id)
            assertEquals(exp.name, act.name)
            assertEquals(exp.lastName, act.lastName)
            assertEquals(exp.phone, act.phone)
            assertEquals(exp.email, act.email)
            assertEquals(exp.userId, act.userId)
        }
    }

    @Test
    fun `should return clients list when user has read permission only`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            grantPermission(ObjectPermission.READ)
            coEvery { clientDataSource.getClients(businessId) } returns emptyList()
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when user has no permission for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            grantPermission(null)
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.clientDataSource.getClients(any()) }
    }

    @Test
    fun `should assert permission against the requested business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.getClients(businessId) } returns emptyList()
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.businessDataSource.getPermission(requestUserId, businessId) }
    }
}
