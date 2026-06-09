package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
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

internal class GetBusinessByIdImplTest {

    private val businessDataSource = mockk<BusinessDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val sut = GetBusinessByIdImpl(businessDataSource, transactionManager)

    @Test
    fun `should return business when exists`() = runUnitTest {
        given()
        val id = Uuid.random()
        val business = Business(id, "Test", "Desc", "Addr", null, "USD", emptyList())
        
        coEvery { transactionManager.transaction<Business>(any()) } coAnswers {
            Result.success(firstArg<suspend () -> Business>().invoke())
        }
        coEvery { businessDataSource.getBusinessById(id) } returns business

        whenn()
        val result = sut(id)

        then()
        assertTrue(result.isSuccess)
        assertEquals(business, result.getOrNull())
    }

    @Test
    fun `should return failure when not exists`() = runUnitTest {
        given()
        val id = Uuid.random()
        
        coEvery { transactionManager.transaction<Business>(any()) } coAnswers {
            Result.failure(GetBusinessById.Error.NotFound())
        }
        coEvery { businessDataSource.getBusinessById(id) } returns null

        whenn()
        val result = sut(id)

        then()
        assertTrue(result.isFailure)
        assertEquals(GetBusinessById.Error.NotFound(), result.exceptionOrNull())
    }
}
