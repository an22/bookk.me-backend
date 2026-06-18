package com.bookk.auth.domain.impl.operation.token

import com.bookk.auth.domain.api.token.entity.SigningKey
import com.bookk.auth.domain.api.token.entity.SigningKeyStatus
import com.bookk.auth.domain.datasource.SigningKeyDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class GetActiveSigningKeyImplTest {

    private class SutFixture {
        val signingKeyDataSource = mockk<SigningKeyDataSource>()
        val transactionManager = mockk<TransactionManager>()

        val sut = GetActiveSigningKeyImpl(
            signingKeyDataSource,
            transactionManager
        )
    }

    @Test
    fun `should return the existing active key without creating a new one`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val activeKey = SigningKey(
            id = Uuid.random(),
            publicKeyPem = "public",
            privateKeyPem = "private",
            status = SigningKeyStatus.ACTIVE,
            createdAt = Clock.System.now(),
            retiredAt = null
        )

        with(fixture) {
            coEvery { signingKeyDataSource.getActiveKey() } returns activeKey
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke()

        then()
        assertEquals(activeKey, result.getOrThrow())
        coVerify(exactly = 0) { fixture.signingKeyDataSource.insertKey(any(), any()) }
    }

    @Test
    fun `should generate and insert a new key when none exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val generatedKey = SigningKey(
            id = Uuid.random(),
            publicKeyPem = "public",
            privateKeyPem = "private",
            status = SigningKeyStatus.ACTIVE,
            createdAt = Clock.System.now(),
            retiredAt = null
        )

        with(fixture) {
            coEvery { signingKeyDataSource.getActiveKey() } returns null
            coEvery { signingKeyDataSource.insertKey(any(), any()) } returns generatedKey
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke()

        then()
        assertEquals(generatedKey, result.getOrThrow())
        coVerify(exactly = 1) { fixture.signingKeyDataSource.insertKey(any(), any()) }
    }
}
