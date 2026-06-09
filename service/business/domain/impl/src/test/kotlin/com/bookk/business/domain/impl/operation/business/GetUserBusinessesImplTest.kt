package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.UserBusinesses
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

internal class GetUserBusinessesImplTest {

    private val businessDataSource = mockk<BusinessDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val sut = GetUserBusinessesImpl(businessDataSource, transactionManager)

    @Test
    fun `should return user businesses when exist`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val userBusinesses = UserBusinesses(Uuid.random(), listOf(Business(Uuid.random(), "Name", "Desc", "Addr", null, "USD", emptyList())))
        
        coEvery { transactionManager.transaction<UserBusinesses>(any()) } coAnswers {
            Result.success(firstArg<suspend () -> UserBusinesses>().invoke())
        }
        coEvery { businessDataSource.getUserBusinesses(userId) } returns userBusinesses

        whenn()
        val result = sut(userId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(userBusinesses, result.getOrNull())
    }
}
