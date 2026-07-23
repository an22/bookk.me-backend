package com.bookk.user.domain.impl.operation

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.user.domain.api.entity.UserEditModel
import com.bookk.user.domain.api.operation.EditUser
import com.bookk.user.domain.datasource.UserDataSource
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EditUserImplTest {

    private class SutFixture {
        val userDataSource = mockk<UserDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = EditUserImpl(userDataSource, transactionManager)
    }

    @Test
    fun `should edit user successfully when datasource confirms update`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val editModel = UserEditModel(firstName = "Jane", lastName = "Smith")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { userDataSource.updateUser(userId, editModel) } returns true
        }

        whenn()
        val result = fixture.sut.invoke(userId, editModel)

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return UserNotFound when datasource returns false`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val editModel = UserEditModel(firstName = "Jane")
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { userDataSource.updateUser(userId, editModel) } returns false
        }

        whenn()
        val result = fixture.sut.invoke(userId, editModel)

        then()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EditUser.Error.UserNotFound)
    }
}
