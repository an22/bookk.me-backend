package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class DeleteModuleImplTest {

    private val dataSource = mockk<AppointmentSubscriptionDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private fun sut() = DeleteModuleImpl(
        dataSource,
        transactionManager
    )

    @Test
    fun `should delete module successfully`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val businessId = Uuid.random()

        coEvery { dataSource.detachBusiness(businessId) } returns Unit

        whenn()
        val result = sut().invoke(businessId)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return failure when detach throws`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val businessId = Uuid.random()

        coEvery { dataSource.detachBusiness(businessId) } answers { throw Error.DatabaseError("", IllegalStateException()) }

        whenn()
        val result = sut().invoke(businessId)

        then()
        assertTrue(result.isFailure)
    }
}
