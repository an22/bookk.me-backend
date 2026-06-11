package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.UserBusinesses
import com.bookk.business.domain.datasource.BusinessDataSource
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
        val transactionManager = mockk<TransactionManager>()
        val sut = GetUserBusinessesImpl(businessDataSource, transactionManager)
    }

    @Test
    fun `should return user businesses when exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val userBusinesses = UserBusinesses(Uuid.random(), listOf(Business(Uuid.random(), "Name", "Desc", "Addr", null, "USD", emptyList())))
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.getUserBusinesses(userId) } returns userBusinesses
        }

        whenn()
        val result = fixture.sut(userId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(userBusinesses, result.getOrNull())
    }
}
