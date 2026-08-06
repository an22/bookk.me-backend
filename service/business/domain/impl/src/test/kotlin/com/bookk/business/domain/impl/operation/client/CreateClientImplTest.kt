package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.api.client.entity.toRemote
import com.bookk.business.domain.api.client.operation.CreateClient
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

internal class CreateClientImplTest {

    private val requestUserId = Uuid.random()

    private class SutFixture {
        val clientDataSource = mockk<ClientDataSource>()
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = CreateClientImpl(transactionManager, clientDataSource, businessDataSource)

        init {
            coEvery { businessDataSource.getPermission(any(), any()) } returns ObjectPermission.OWNER.int
        }

        fun grantPermission(permission: ObjectPermission?) {
            coEvery { businessDataSource.getPermission(any(), any()) } returns permission?.int
        }
    }

    @Test
    fun `should create detached client successfully when valid data provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "Doe", "123456", "john@doe.com")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.getClient(businessId, client.phone) } returns null
            coEvery { clientDataSource.createDetachedClient(businessId, client) } returns client
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isSuccess)
        val actual = result.getOrNull()
        val expected = client.toRemote()
        assertEquals(expected.id, actual?.id)
        assertEquals(expected.name, actual?.name)
        assertEquals(expected.lastName, actual?.lastName)
        assertEquals(expected.phone, actual?.phone)
        assertEquals(expected.email, actual?.email)
        assertEquals(expected.userId, actual?.userId)
    }

    @Test
    fun `should create integrated client successfully when valid data provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Integrated(Uuid.random(), "John", "Doe", "123456", "john@doe.com", Uuid.random())
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.getClient(businessId, client.phone) } returns null
            coEvery { clientDataSource.createIntegratedClient(businessId, client) } returns client
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isSuccess)
        val actual = result.getOrNull()
        val expected = client.toRemote()
        assertEquals(expected.id, actual?.id)
        assertEquals(expected.name, actual?.name)
        assertEquals(expected.lastName, actual?.lastName)
        assertEquals(expected.phone, actual?.phone)
        assertEquals(expected.email, actual?.email)
        assertEquals(expected.userId, actual?.userId)
    }

    @Test
    fun `should return error when client already exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "Doe", "123456", "john@doe.com")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.getClient(businessId, client.phone) } returns client
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateClient.Error.ClientExist)
    }

    @Test
    fun `should return validation error when name is too long`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "A".repeat(513), "Doe", "123456", "john@doe.com")
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateClient.Error.ClientValidationError)
    }

    @Test
    fun `should return failure when user has read permission only`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "Doe", "123456", "john@doe.com")
        with(fixture) {
            transactionManager.mockTransaction()
            grantPermission(ObjectPermission.READ)
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.clientDataSource.createDetachedClient(any(), any()) }
    }

    @Test
    fun `should return failure when user has no permission for the business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "Doe", "123456", "john@doe.com")
        with(fixture) {
            transactionManager.mockTransaction()
            grantPermission(null)
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
        coVerify(exactly = 0) { fixture.clientDataSource.createDetachedClient(any(), any()) }
    }

    @Test
    fun `should assert permission against the requested business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "Doe", "123456", "john@doe.com")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.getClient(businessId, client.phone) } returns null
            coEvery { clientDataSource.createDetachedClient(businessId, client) } returns client
        }

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.businessDataSource.getPermission(requestUserId, businessId) }
    }

    @Test
    fun `should return validation error when last name is too long`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "A".repeat(513), "123456", "john@doe.com")
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateClient.Error.ClientValidationError)
    }

    @Test
    fun `should return validation error when phone contains letters`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "Doe", "call-me-maybe", "john@doe.com")
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateClient.Error.ClientValidationError)
    }

    @Test
    fun `should return validation error when phone is too short`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "Doe", "12", "john@doe.com")
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(requestUserId, businessId, client)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateClient.Error.ClientValidationError)
    }
}
