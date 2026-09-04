package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.domain.api.service.operation.UpdateService
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
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

internal class UpdateServiceImplTest {

    private class SutFixture {
        val serviceDataSource = mockk<ServiceDataSource>()
        val businessPermissionDataSource = mockk<BusinessPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateServiceImpl(serviceDataSource, businessPermissionDataSource, transactionManager)
    }

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
    fun `should return success when update service with valid data`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val service = createTestService()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessPermissionDataSource.getPermission(userId, service.businessId) } returns ObjectPermission.EDIT.int
            coEvery { serviceDataSource.editService(service) } returns service
        }

        whenn()
        val result = fixture.sut(userId, service)

        then()
        assertTrue(result.isSuccess)
        assertEquals(service, result.getOrNull())
    }

    @Test
    fun `should return failure when name is blank`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val service = createTestService(name = "")

        whenn()
        val result = fixture.sut(userId, service)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateService.Error.ValidationError)
    }

    @Test
    fun `should return failure when permission denied`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val service = createTestService()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessPermissionDataSource.getPermission(userId, service.businessId) } returns ObjectPermission.READ.int
        }

        whenn()
        val result = fixture.sut(userId, service)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should return failure when service already exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val service = createTestService()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessPermissionDataSource.getPermission(userId, service.businessId) } returns ObjectPermission.EDIT.int
            coEvery { serviceDataSource.editService(service) } throws Error.UniqueConstraintFailed("", RuntimeException())
        }

        whenn()
        val result = fixture.sut(userId, service)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UpdateService.Error.ServiceExist)
    }
}
