package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.api.client.entity.toRemote
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

internal class GetClientsImplTest {

    private class SutFixture {
        val clientDataSource = mockk<ClientDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetClientsImpl(clientDataSource, transactionManager)
    }

    @Test
    fun `should return clients list when clients exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val clients = listOf(
            Client.Detached(Uuid.random(), "John", "Doe", "123456", "john@doe.com"),
            Client.Integrated(Uuid.random(), "Jane", "Doe", "654321", "jane@doe.com", Uuid.random())
        )
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.getClients(businessId) } returns clients
        }

        whenn()
        val result = fixture.sut(businessId)

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
}
