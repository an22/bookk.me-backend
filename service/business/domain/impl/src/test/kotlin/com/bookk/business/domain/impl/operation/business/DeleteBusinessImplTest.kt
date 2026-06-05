package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class DeleteBusinessImplTest {

    private val businessDataSource = mockk<BusinessDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val eventProducer = mockk<StandardEventProducer>(relaxed = true)
    private val sut = DeleteBusinessImpl(businessDataSource, transactionManager, eventProducer)

    @Test
    fun `should delete business successfully and send events when user has businesses`() = runUnitTest {
        given()
        val userId = Uuid.random()
        val deletedIds = listOf(Uuid.random(), Uuid.random())
        
        coEvery { transactionManager.transaction<Unit>(any()) } coAnswers {
            Result.success(firstArg<suspend () -> Unit>().invoke())
        }
        coEvery { businessDataSource.deleteUserBusinesses(userId) } returns deletedIds

        whenn()
        val result = sut(userId)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { eventProducer.send(any(), any()) }
    }
}
