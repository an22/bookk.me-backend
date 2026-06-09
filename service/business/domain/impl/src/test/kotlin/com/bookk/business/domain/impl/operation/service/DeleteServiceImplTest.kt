package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class DeleteServiceImplTest {

    private val serviceDataSource = mockk<ServiceDataSource>(relaxed = true)
    private val businessDataSource = mockk<BusinessDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val sut = DeleteServiceImpl(serviceDataSource, businessDataSource, transactionManager)

    @Test
    fun `should return success when delete service with valid data`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val id = Uuid.random()
        
        coEvery { businessDataSource.getPermission(userId, businessId) } returns ObjectPermission.EDIT.int

        whenn()
        val result = sut(userId, businessId, id)

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { serviceDataSource.deleteService(id) }
    }
}
