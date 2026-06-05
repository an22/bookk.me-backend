package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class UpdateBusinessImplTest {

    private val businessDataSource = mockk<BusinessDataSource>(relaxed = true)
    private val transactionManager = mockk<TransactionManager>()
    private val sut = UpdateBusinessImpl(businessDataSource, transactionManager)

    @Test
    fun `should return success when truncated values are provided`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val businessId = Uuid.random()
        val updateModel = BusinessUpdateModel(
            id = businessId,
            name = "A".repeat(1000),
            description = "D".repeat(2000),
            address = "Addr".repeat(200),
            location = null,
            currencyCode = "USDD",
            socials = listOf(Business.Social(Business.SocialKind.INSTAGRAM, "V".repeat(500)))
        )
        
        whenn()
        val result = sut(updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { 
            businessDataSource.updateBusiness(match {
                it.name?.length == Business.MAX_NAME_LENGTH &&
                it.description?.length == Business.MAX_DESCRIPTION_LENGTH &&
                it.address?.length == Business.MAX_ADDRESS_LENGTH &&
                it.currencyCode?.length == Business.MAX_CURRENCY_CODE &&
                it.socials?.first()?.value?.length == Business.MAX_SOCIAL_LENGTH
            })
        }
    }

    @Test
    fun `should return success when null values are provided`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val updateModel = BusinessUpdateModel(Uuid.random(), null, null, null, null, null, null)
        
        whenn()
        val result = sut(updateModel)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { 
            businessDataSource.updateBusiness(match {
                it.name == null && it.description == null && it.address == null && it.socials == null
            })
        }
    }
}
