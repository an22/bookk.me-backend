package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
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

internal class UpdateBusinessImplTest {

    private class SutFixture {
        val businessDataSource = mockk<BusinessDataSource>(relaxed = true)
        val transactionManager = mockk<TransactionManager>()
        val eventProducer = mockk<StandardEventProducer>(relaxed = true)
        val sut = UpdateBusinessImpl(businessDataSource, transactionManager, eventProducer)
    }

    @Test
    fun `should return success when truncated values are provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val updateModel = BusinessUpdateModel(
            id = businessId,
            name = "A".repeat(1000),
            description = "D".repeat(2000),
            address = "Addr".repeat(200),
            location = null,
            currencyCode = "USDD",
            timeZone = TimeZone.UTC,
            socials = listOf(Business.Social(Business.SocialKind.INSTAGRAM, "V".repeat(500)))
        )
        
        whenn()
        val result = fixture.sut(updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.updateBusiness(match {
                it.name?.length == Business.MAX_NAME_LENGTH &&
                it.description?.length == Business.MAX_DESCRIPTION_LENGTH &&
                it.address?.length == Business.MAX_ADDRESS_LENGTH &&
                it.currencyCode?.length == Business.MAX_CURRENCY_CODE &&
                it.socials?.firstOrNull()?.value?.length == Business.MAX_SOCIAL_LENGTH
            })
        }
    }

    @Test
    fun `should return success when null values are provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val updateModel = BusinessUpdateModel(Uuid.random(), null, null, null, null, null, null, emptyList())

        whenn()
        val result = fixture.sut(updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.businessDataSource.updateBusiness(match {
                it.name == null && it.description == null && it.address == null && it.socials?.isEmpty() == true
            })
        }
    }

    @Test
    fun `should publish business updated event with the updated business`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val updatedBusiness = Business.stub(id = businessId, name = "New Name", address = "New Address")
        coEvery { fixture.businessDataSource.updateBusiness(any()) } returns updatedBusiness
        val updateModel = BusinessUpdateModel(businessId, "New Name", null, "New Address", null, null, TimeZone.UTC, emptyList())

        whenn()
        val result = fixture.sut(updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.eventProducer.send(match<BusinessEvent.Updated> {
                it.business == BusinessEvent.BusinessDTO(
                    id = updatedBusiness.id,
                    name = updatedBusiness.name,
                    address = updatedBusiness.address,
                    timeZone = updatedBusiness.timeZone
                )
            }, any())
        }
    }
}
