package com.bookk.business.domain.impl.operation.business

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

internal class GetBusinessPermissionImplTest {

    private class SutFixture {
        val businessDataSource = mockk<BusinessDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetBusinessPermissionImpl(businessDataSource, transactionManager)
    }

    private val userId = Uuid.random()
    private val businessId = Uuid.random()

    @Test
    fun `should return the stored permission`() = runUnitTest {
        given()
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(userId, businessId) } returns ObjectPermission.OWNER.int
        }

        whenn()
        val result = fixture.sut.invoke(userId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(ObjectPermission.OWNER, result.getOrNull())
    }

    @Test
    fun `should return no permission when the user holds none`() = runUnitTest {
        given()
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(userId, businessId) } returns null
        }

        whenn()
        val result = fixture.sut.invoke(userId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(ObjectPermission.NONE, result.getOrNull())
    }

    @Test
    fun `should return no permission when the stored value matches no permission level`() = runUnitTest {
        given()
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(userId, businessId) } returns 42
        }

        whenn()
        val result = fixture.sut.invoke(userId, businessId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(ObjectPermission.NONE, result.getOrNull())
    }

    @Test
    fun `should return failure when the datasource fails`() = runUnitTest {
        given()
        val fixture = SutFixture()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getPermission(userId, businessId) } throws RuntimeException("db error")
        }

        whenn()
        val result = fixture.sut.invoke(userId, businessId)

        then()
        assertTrue(result.isFailure)
    }
}
