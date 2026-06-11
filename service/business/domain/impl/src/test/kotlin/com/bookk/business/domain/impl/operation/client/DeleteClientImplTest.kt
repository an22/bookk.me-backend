package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.operation.DeleteClient
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class DeleteClientImplTest {

    private class SutFixture {
        val clientDataSource = mockk<ClientDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = DeleteClientImpl(transactionManager, clientDataSource)
    }

    @Test
    fun `should return success when delete client and client exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val id = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.deleteClient(businessId, id) } returns true
        }

        whenn()
        val result = fixture.sut(businessId, id)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when delete client and client not exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val id = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { clientDataSource.deleteClient(businessId, id) } returns false
        }

        whenn()
        val result = fixture.sut(businessId, id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DeleteClient.Error.NotFound)
    }
}
