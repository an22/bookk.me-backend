package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.api.client.entity.toRemote
import com.bookk.business.domain.api.client.operation.CreateClient
import com.bookk.business.domain.datasource.ClientDataSource
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
import kotlin.uuid.Uuid

internal class CreateClientImplTest {

    private class SutFixture {
        val clientDataSource = mockk<ClientDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = CreateClientImpl(transactionManager, clientDataSource)
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
        val result = fixture.sut(businessId, client)

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
        val result = fixture.sut(businessId, client)

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
        val result = fixture.sut(businessId, client)

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
        val result = fixture.sut(businessId, client)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateClient.Error.ClientValidationError)
    }

    @Test
    fun `should return validation error when last name is too long`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val client = Client.Detached(Uuid.random(), "John", "A".repeat(513), "123456", "john@doe.com")
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(businessId, client)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateClient.Error.ClientValidationError)
    }
}
