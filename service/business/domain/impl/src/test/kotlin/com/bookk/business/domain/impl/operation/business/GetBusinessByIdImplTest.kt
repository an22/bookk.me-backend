package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import library.permissions.ResourcePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class GetBusinessByIdImplTest {

    private class SutFixture {
        val businessDataSource = mockk<BusinessDataSource>()
        val businessPermissionDataSource = mockk<BusinessPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetBusinessByIdImpl(businessDataSource, businessPermissionDataSource, transactionManager)
    }

    @Test
    fun `should return business when exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val id = Uuid.random()
        val business = Business.stub(id = id, name = "Test")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getBusinessById(id) } returns business
        }

        whenn()
        val result = fixture.sut(id)

        then()
        assertTrue(result.isSuccess)
        assertEquals(business, result.getOrNull())
    }

    @Test
    fun `should attach the requesting user's permissions when a requesting user is given`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val id = Uuid.random()
        val userId = Uuid.random()
        val business = Business.stub(id = id, name = "Test")
        val permissions = BusinessPermissions.stub(business = ResourcePermission.FULL)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getBusinessById(id) } returns business
            coEvery { businessPermissionDataSource.getPermissions(userId, id) } returns permissions
        }

        whenn()
        val result = fixture.sut(id, userId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(permissions, result.getOrNull()?.permissions)
    }

    @Test
    fun `should return failure when not exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val id = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getBusinessById(id) } returns null
        }

        whenn()
        val result = fixture.sut(id)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GetBusinessById.Error.NotFound)
    }
}
