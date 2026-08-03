package com.bookk.business.domain.impl.operation.user

import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
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
import kotlin.uuid.Uuid

internal class SyncUserProfileImplTest {

    private class SutFixture {
        val clientDataSource = mockk<ClientDataSource>()
        val employeeDataSource = mockk<EmployeeDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = SyncUserProfileImpl(transactionManager, clientDataSource, employeeDataSource)
    }

    @Test
    fun `should sync profile to integrated clients when matching user exists`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                clientDataSource.updateIntegratedClients(userId, "John", "Doe", "123456", "john@doe.com")
            } returns 1
            coEvery {
                employeeDataSource.updateIntegratedEmployees(userId, "John", "Doe", "123456", "john@doe.com")
            } returns 0
        }

        whenn()
        val result = fixture.sut(userId, "John", "Doe", "123456", "john@doe.com")

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.clientDataSource.updateIntegratedClients(userId, "John", "Doe", "123456", "john@doe.com")
        }
    }

    @Test
    fun `should sync profile to employees linked to user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                clientDataSource.updateIntegratedClients(userId, "John", "Doe", "123456", "john@doe.com")
            } returns 0
            coEvery {
                employeeDataSource.updateIntegratedEmployees(userId, "John", "Doe", "123456", "john@doe.com")
            } returns 1
        }

        whenn()
        val result = fixture.sut(userId, "John", "Doe", "123456", "john@doe.com")

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.employeeDataSource.updateIntegratedEmployees(userId, "John", "Doe", "123456", "john@doe.com")
        }
    }

    @Test
    fun `should succeed as no-op when no integrated client or employee matches user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery {
                clientDataSource.updateIntegratedClients(userId, "John", "Doe", "123456", "john@doe.com")
            } returns 0
            coEvery {
                employeeDataSource.updateIntegratedEmployees(userId, "John", "Doe", "123456", "john@doe.com")
            } returns 0
        }

        whenn()
        val result = fixture.sut(userId, "John", "Doe", "123456", "john@doe.com")

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            fixture.clientDataSource.updateIntegratedClients(userId, "John", "Doe", "123456", "john@doe.com")
        }
        coVerify(exactly = 1) {
            fixture.employeeDataSource.updateIntegratedEmployees(userId, "John", "Doe", "123456", "john@doe.com")
        }
    }
}
