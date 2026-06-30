package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.IssueQuote
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import library.signing.GetActiveSigningKey
import library.signing.SigningKey
import library.signing.SigningKeyStatus
import library.signing.impl.key.RsaSigningKeyFactory
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class IssueQuoteImplTest {

    private class SutFixture {
        val serviceDataSource = mockk<ServiceDataSource>()
        val getActiveSigningKey = mockk<GetActiveSigningKey>()
        val transactionManager = mockk<TransactionManager>()
        val sut = IssueQuoteImpl(serviceDataSource, getActiveSigningKey, transactionManager)
    }

    private fun realSigningKey(): SigningKey {
        val (publicKeyPem, privateKeyPem) = RsaSigningKeyFactory.generate()
        return SigningKey(
            id = Uuid.random(),
            publicKeyPem = publicKeyPem,
            privateKeyPem = privateKeyPem,
            status = SigningKeyStatus.ACTIVE,
            createdAt = Clock.System.now(),
            retiredAt = null
        )
    }

    @Test
    fun `should return quote with services and signed token when all service ids exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val services = listOf(
            Service.stub(businessId = businessId),
            Service.stub(businessId = businessId)
        )
        val serviceIds = services.map { it.id }
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { serviceDataSource.getServicesByIds(serviceIds) } returns services
            coEvery { getActiveSigningKey() } returns Result.success(realSigningKey())
        }

        whenn()
        val result = fixture.sut(businessId, serviceIds)

        then()
        assertTrue(result.isSuccess)
        val quote = result.getOrThrow()
        assertTrue(quote.services == services)
        assertNotNull(quote.token)
        assertTrue(quote.token.isNotBlank())
    }

    @Test
    fun `should return ServiceNotFound when a requested service id is missing`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val businessId = Uuid.random()
        val existingService = Service.stub(businessId = businessId)
        val missingId = Uuid.random()
        val serviceIds = listOf(existingService.id, missingId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { serviceDataSource.getServicesByIds(serviceIds) } returns listOf(existingService)
            coEvery { getActiveSigningKey() } returns Result.success(realSigningKey())
        }

        whenn()
        val result = fixture.sut(businessId, serviceIds)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IssueQuote.Error.ServiceNotFound)
    }
}
