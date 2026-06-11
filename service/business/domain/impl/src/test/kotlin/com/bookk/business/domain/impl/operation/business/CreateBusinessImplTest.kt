package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class CreateBusinessImplTest {

    private class SutFixture {
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = CreateBusinessImpl(businessDataSource, transactionManager)
    }

    @Test
    fun `should create business successfully when valid data provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val business = Business(Uuid.random(), "Name", "Desc", "Addr", null, "USD", emptyList())
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.isBusinessExist(userId) } returns false
            coEvery { businessDataSource.createBusiness(userId, "Name", "USD") } returns business
            coEvery { businessDataSource.setUserPermissions(userId, business.id, ObjectPermission.OWNER.int) } returns Unit
        }

        whenn()
        val result = fixture.sut(userId, "Name", "USD")

        then()
        assertTrue(result.isSuccess)
        assertEquals(business, result.getOrNull())
    }

    @Test
    fun `should return failure when name is too short`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(Uuid.random(), "A", "USD")

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateBusiness.Error.BusinessValidationError)
    }

    @Test
    fun `should return failure when name is too long`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(Uuid.random(), "A".repeat(513), "USD")

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateBusiness.Error.BusinessValidationError)
    }

    @Test
    fun `should return failure when business exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.isBusinessExist(userId) } returns true
        }

        whenn()
        val result = fixture.sut(userId, "Name", "USD")

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateBusiness.Error.BusinessExist)
    }
}
