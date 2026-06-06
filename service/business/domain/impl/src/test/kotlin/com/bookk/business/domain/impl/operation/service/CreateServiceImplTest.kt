package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.joda.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CreateServiceImplTest {

    private val serviceDataSource = mockk<ServiceDataSource>()
    private val businessDataSource = mockk<BusinessDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val sut = CreateServiceImpl(serviceDataSource, businessDataSource, transactionManager)

    private fun createTestService(name: String = "Service") = Service(
        id = Uuid.random(),
        businessId = Uuid.random(),
        group = ServiceGroup(Uuid.random(), Uuid.random(), "Group", Instant.fromEpochMilliseconds(0)),
        name = name,
        duration = 30.minutes,
        price = Money.parse("USD 100"),
        isAvailable = true,
        createdAt = Instant.fromEpochMilliseconds(0)
    )

    @Test
    fun `should create service successfully when valid data provided`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val service = createTestService()
        
        coEvery { businessDataSource.getPermission(userId, service.businessId) } returns ObjectPermission.EDIT.int
        coEvery { serviceDataSource.createService(service) } returns service

        whenn()
        val result = sut(userId, service)

        then()
        assertTrue(result.isSuccess)
        assertEquals(service, result.getOrNull())
    }

    @Test
    fun `should return failure when name is blank`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val service = createTestService(name = "")
        
        whenn()
        val result = sut(userId, service)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateService.Error.ValidationError)
    }

    @Test
    fun `should return failure when permission denied`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val service = createTestService()
        
        coEvery { businessDataSource.getPermission(userId, service.businessId) } returns ObjectPermission.READ.int

        whenn()
        val result = sut(userId, service)

        then()
        assertTrue(result.isFailure)
        // library.permissions.assert throws PermissionDenied if check fails
    }
}
