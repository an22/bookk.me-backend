package com.bookk.user.data.datasource

import com.bookk.core.data.cache.test.InMemoryCacheClient
import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.user.data.orm.table.UserTable
import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.entity.UserEditModel
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class UserDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(UserTable)
        val cacheClient = InMemoryCacheClient()
        val sut = UserDataSourceImpl(cacheClient)
    }

    @Test
    fun `should insert user and retrieve by id`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val user = User.stub()

        whenn()
        val created = suspendTransaction { fixture.sut.insertNewUser(user) }
        val found = suspendTransaction { fixture.sut.getUserById(created.id) }

        then()
        assertNotNull(found)
        assertEquals("Alice", found!!.name)
        assertEquals(user.email, found.email)
    }

    @Test
    fun `should return null when user not found by id`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val found = suspendTransaction { fixture.sut.getUserById(Uuid.random()) }

        then()
        assertNull(found)
    }

    @Test
    fun `should retrieve user by email`() = runUnitTest {
        given()
        val fixture = SutFixture()
        suspendTransaction { fixture.sut.insertNewUser(User.stub(email = "find-me@example.com")) }

        whenn()
        val found = suspendTransaction { fixture.sut.getUserByEmail("find-me@example.com") }

        then()
        assertNotNull(found)
        assertEquals("Alice", found!!.name)
    }

    @Test
    fun `should return null when user not found by email`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val found = suspendTransaction { fixture.sut.getUserByEmail("nobody@example.com") }

        then()
        assertNull(found)
    }

    @Test
    fun `should update user name and return true`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.insertNewUser(User.stub()) }

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateUser(created.id, UserEditModel(firstName = "Bob"))
        }
        val found = suspendTransaction { fixture.sut.getUserById(created.id) }

        then()
        assertTrue(updated)
        assertEquals("Bob", found!!.name)
    }

    @Test
    fun `should return false when updating non-existent user`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val updated = suspendTransaction {
            fixture.sut.updateUser(Uuid.random(), UserEditModel(firstName = "Bob"))
        }

        then()
        assertTrue(!updated)
    }

    @Test
    fun `should delete user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val created = suspendTransaction { fixture.sut.insertNewUser(User.stub()) }

        whenn()
        suspendTransaction { fixture.sut.deleteUser(created.id) }
        val found = suspendTransaction { fixture.sut.getUserById(created.id) }

        then()
        assertNull(found)
    }
}
