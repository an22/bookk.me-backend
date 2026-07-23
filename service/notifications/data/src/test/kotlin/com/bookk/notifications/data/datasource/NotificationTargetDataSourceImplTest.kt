package com.bookk.notifications.data.datasource

import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.notifications.data.orm.entity.NotificationEmailTargetEntity
import com.bookk.notifications.data.orm.entity.NotificationTelegramTargetEntity
import com.bookk.notifications.data.orm.table.NotificationEmailTargetTable
import com.bookk.notifications.data.orm.table.NotificationTelegramTargetTable
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class NotificationTargetDataSourceImplTest {

    private class SutFixture {
        val db = createTestDatabase(NotificationEmailTargetTable, NotificationTelegramTargetTable)
        val sut = NotificationTargetDataSourceImpl()

        suspend fun insertEmail(userId: Uuid, email: String) {
            suspendTransaction {
                NotificationEmailTargetEntity.new {
                    this.userId = userId.toJavaUuid()
                    this.email = email
                }
            }
        }

        suspend fun insertTelegram(userId: Uuid, tag: String) {
            suspendTransaction {
                NotificationTelegramTargetEntity.new {
                    this.userId = userId.toJavaUuid()
                    this.telegramTag = tag
                }
            }
        }
    }

    @Test
    fun `should retrieve email for user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        fixture.insertEmail(userId, "user@example.com")

        whenn()
        val email = suspendTransaction { fixture.sut.getEmail(userId) }

        then()
        assertEquals("user@example.com", email)
    }

    @Test
    fun `should return null email for unknown user`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val email = suspendTransaction { fixture.sut.getEmail(Uuid.random()) }

        then()
        assertNull(email)
    }

    @Test
    fun `should retrieve telegram tag for user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        fixture.insertTelegram(userId, "@myuser")

        whenn()
        val telegram = suspendTransaction { fixture.sut.getTelegram(userId) }

        then()
        assertEquals("@myuser", telegram)
    }

    @Test
    fun `should return null telegram for unknown user`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val telegram = suspendTransaction { fixture.sut.getTelegram(Uuid.random()) }

        then()
        assertNull(telegram)
    }

    @Test
    fun `should insert email via upsertEmail`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()

        whenn()
        suspendTransaction { fixture.sut.upsertEmail(userId, "inserted@example.com") }

        then()
        val email = suspendTransaction { fixture.sut.getEmail(userId) }
        assertEquals("inserted@example.com", email)
    }

    @Test
    fun `should update existing email via upsertEmail`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        fixture.insertEmail(userId, "old@example.com")

        whenn()
        suspendTransaction { fixture.sut.upsertEmail(userId, "new@example.com") }

        then()
        val email = suspendTransaction { fixture.sut.getEmail(userId) }
        assertEquals("new@example.com", email)
    }

    @Test
    fun `should insert telegram via upsertTelegram`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()

        whenn()
        suspendTransaction { fixture.sut.upsertTelegram(userId, "@inserted") }

        then()
        val telegram = suspendTransaction { fixture.sut.getTelegram(userId) }
        assertEquals("@inserted", telegram)
    }

    @Test
    fun `should update existing telegram via upsertTelegram`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        fixture.insertTelegram(userId, "@old")

        whenn()
        suspendTransaction { fixture.sut.upsertTelegram(userId, "@new") }

        then()
        val telegram = suspendTransaction { fixture.sut.getTelegram(userId) }
        assertEquals("@new", telegram)
    }
}
