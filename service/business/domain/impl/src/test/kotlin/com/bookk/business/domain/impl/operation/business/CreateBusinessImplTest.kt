package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessCreateRequest
import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.BusinessPermissionDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.api.UserSnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.TimeZone
import library.permissions.ResourcePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class CreateBusinessImplTest {

    private class SutFixture {
        val businessDataSource = mockk<BusinessDataSource>()
        val businessPermissionDataSource = mockk<BusinessPermissionDataSource>()
        val employeeDataSource = mockk<EmployeeDataSource>()
        val userClient = mockk<UserClient>()
        val transactionManager = mockk<TransactionManager>()
        val sut = CreateBusinessImpl(businessDataSource, businessPermissionDataSource, employeeDataSource, userClient, transactionManager)
    }

    @Test
    fun `should create business successfully when valid data provided`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val business = Business.stub(name = "Name", currencyCode = "USD")
        val user = UserSnapshot.stub(id = userId, name = "Owner", lastName = "Ownerson", email = "owner@example.com", phone = "+10000000000")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.isBusinessExist(userId) } returns false
            coEvery { businessDataSource.createBusiness(userId, "Name", "USD", TimeZone.UTC) } returns business
            coEvery { businessPermissionDataSource.setPermission(userId, business.id, any(), any()) } returns Unit
            coEvery { userClient.getUserById(userId) } returns Result.success(user)
            coEvery { employeeDataSource.createEmployee(any()) } returns Employee.stub(businessId = business.id, userId = userId)
        }

        whenn()
        val result = fixture.sut(userId, BusinessCreateRequest(name = "Name", currencyCode = "USD", timeZone = TimeZone.UTC))

        then()
        assertTrue(result.isSuccess)
        assertEquals(business.copy(permissions = BusinessPermissions.FULL), result.getOrNull())
        BusinessResource.entries.forEach { resource ->
            coVerify(exactly = 1) {
                fixture.businessPermissionDataSource.setPermission(userId, business.id, resource, ResourcePermission.FULL)
            }
        }
        coVerify(exactly = 1) {
            fixture.employeeDataSource.createEmployee(
                match {
                    it.businessId == business.id &&
                        it.userId == userId &&
                        it.name == user.name &&
                        it.lastName == user.lastName &&
                        it.email == user.email &&
                        it.phone == user.phone
                }
            )
        }
    }

    @Test
    fun `should return failure when name is too short`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(Uuid.random(), BusinessCreateRequest(name = "A", currencyCode = "USD", timeZone = TimeZone.UTC))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateBusiness.Error.BusinessValidationError)
    }

    @Test
    fun `should return failure when name is too long`() = runUnitTest {
        given()
        val fixture = SutFixture()
        fixture.transactionManager.mockTransaction()

        whenn()
        val result = fixture.sut(Uuid.random(), BusinessCreateRequest(name = "A".repeat(513), currencyCode = "USD", timeZone = TimeZone.UTC))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateBusiness.Error.BusinessValidationError)
    }

    @Test
    fun `should return failure when business exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { businessDataSource.isBusinessExist(userId) } returns true
        }

        whenn()
        val result = fixture.sut(userId, BusinessCreateRequest(name = "Name", currencyCode = "USD", timeZone = TimeZone.UTC))

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CreateBusiness.Error.BusinessExist)
    }
}
