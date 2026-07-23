package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.business.client.api.event.BusinessEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class UpdateBusinessInformationTest {

    private class SutFixture {
        val subscriptionDataSource = mockk<AppointmentSubscriptionDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateBusinessInformation(subscriptionDataSource, transactionManager)
    }

    private fun makeBusinessDTO(id: Uuid = Uuid.random()): BusinessEvent.BusinessDTO = BusinessEvent.BusinessDTO(
        id = id,
        name = "Test Salon",
        address = "123 Main St",
        timeZone = TimeZone.UTC
    )

    @Test
    fun `should update business information from event`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val dto = makeBusinessDTO()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                subscriptionDataSource.updateBusinessInfo(dto.id, dto.name, dto.address, dto.timeZone)
            } returns Unit
        }

        whenn()
        val result = fixture.sut.invoke(dto)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.subscriptionDataSource.updateBusinessInfo(dto.id, dto.name, dto.address, dto.timeZone)
        }
    }

    @Test
    fun `should return failure when datasource throws`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val dto = makeBusinessDTO()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                subscriptionDataSource.updateBusinessInfo(any(), any(), any(), any())
            } throws RuntimeException("db error")
        }

        whenn()
        val result = fixture.sut.invoke(dto)

        then()
        assertTrue(result.isFailure)
    }
}
