package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DeleteDayOffsInThePastImplTest {

    private class SutFixture {
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val transactionManager = mockk<TransactionManager>()

        val sut = DeleteDayOffsInThePastImpl(
            subscriptionDataSource,
            transactionManager
        )
    }

    @Test
    fun `should delete day offs in the past`() = runUnitTest {
        given()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { subscriptionDataSource.deleteDayOffsInThePast() } returns Unit
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke()

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.subscriptionDataSource.deleteDayOffsInThePast() }
    }

    @Test
    fun `should return failure when datasource fails`() = runUnitTest {
        given()
        val fixture = SutFixture()

        with(fixture) {
            coEvery { subscriptionDataSource.deleteDayOffsInThePast() } throws RuntimeException("DB error")
            transactionManager.mockTransaction()
        }

        whenn()
        val result = fixture.sut.invoke()

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "DB error")
    }
}
