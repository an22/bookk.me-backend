package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.UserBusinesses
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class GetUserBusinessesImplTest {

    private class SutFixture {
        val businessDataSource = mockk<BusinessDataSource>()
        val businessPermissionDataSource = mockk<BusinessPermissionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetUserBusinessesImpl(businessDataSource, businessPermissionDataSource, transactionManager)
    }

    @Test
    fun `should return user businesses when exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val business = Business.stub(name = "Name")
        val userBusinesses = UserBusinesses(Uuid.random(), listOf(business))
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getUserBusinesses(userId) } returns userBusinesses
            coEvery { businessPermissionDataSource.getPermissions(userId, business.id) } returns BusinessPermissions.NONE
        }

        whenn()
        val result = fixture.sut(userId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(userBusinesses, result.getOrNull())
    }
}
