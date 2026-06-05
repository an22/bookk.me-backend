package com.bookk.business.domain.impl.operation.service
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.api.service.operation.CreateServiceGroup
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CreateServiceGroupImplTest {

    private val serviceDataSource = mockk<ServiceDataSource>()
    private val businessDataSource = mockk<BusinessDataSource>()
    private val transactionManager = mockk<TransactionManager>()
    private val sut = CreateServiceGroupImpl(serviceDataSource, businessDataSource, transactionManager)

    private fun createTestGroup(name: String = "Group") = ServiceGroup(
        id = Uuid.random(),
        businessId = Uuid.random(),
        name = name,
        createdAt = Instant.fromEpochMilliseconds(0)
    )

    @Test
    fun `should return success when create service group with valid data`() = runUnitTest {
        given()
        transactionManager.mockTransaction()
        val userId = Uuid.random()
        val group = createTestGroup()

        coEvery { businessDataSource.getPermission(userId, group.businessId) } returns ObjectPermission.WRITE.int
        coEvery { serviceDataSource.createServiceGroup(group) } returns group

        whenn()
        val result = sut(userId, group)

        then()
        assertTrue(result.isSuccess)
        assertEquals(group, result.getOrNull())
    }

    @Test
    fun `should return failure when name is blank`() = runUnitTest {
        given()

        val userId = Uuid.random()
        val group = createTestGroup(name = "")
        
        whenn()
        val result = sut(userId, group)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateServiceGroup.Error.ValidationError)
    }
}
