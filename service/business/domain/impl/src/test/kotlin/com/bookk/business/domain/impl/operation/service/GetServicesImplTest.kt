package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import org.joda.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class GetServicesImplTest {

    private val serviceDataSource = mockk<ServiceDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val sut = GetServicesImpl(serviceDataSource, transactionManager)

    @Test
    fun `should return services list when exist`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val services = listOf(
            Service(
                Uuid.random(),
                businessId,
                ServiceGroup(Uuid.random(), businessId, "Group", Instant.fromEpochMilliseconds(0)),
                "Service",
                30.minutes,
                Money.parse("USD 100"),
                true,
                Instant.fromEpochMilliseconds(0)
            )
        )
        
        coEvery { serviceDataSource.getServices(businessId) } returns services

        whenn()
        val result = sut(businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(services, result.getOrNull())
    }
}
