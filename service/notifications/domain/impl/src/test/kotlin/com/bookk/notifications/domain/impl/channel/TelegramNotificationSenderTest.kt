package com.bookk.notifications.domain.impl.channel

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.notifications.domain.datasource.NotificationTargetDataSource
import com.bookk.notifications.domain.impl.notification.EmailNotification
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.notifications.domain.impl.notification.PushNotification
import com.bookk.notifications.domain.impl.notification.TextNotification
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class TelegramNotificationSenderTest {

    private class SutFixture {
        val transactionManager = mockk<TransactionManager>()
        val targetDataSource = mockk<NotificationTargetDataSource>()
        val sut = TelegramNotificationSender(transactionManager, targetDataSource)
    }

    private fun notificationParams() = NotificationParameters(
        type = NotificationType.APPOINTMENT,
        push = PushNotification(title = "Title", body = "Subtitle"),
        email = EmailNotification(subject = "Subject", body = "Body"),
        text = TextNotification(text = "Text"),
    )

    @Test
    fun `should fetch telegram target and return success`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { targetDataSource.getTelegram(userId) } returns "@user_tag"
        }

        whenn()
        val result = fixture.sut.send(userId, notificationParams())

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.targetDataSource.getTelegram(userId) }
    }

    @Test
    fun `should return success when telegram target is not configured`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { targetDataSource.getTelegram(userId) } returns null
        }

        whenn()
        val result = fixture.sut.send(userId, notificationParams())

        then()
        assertTrue(result.isSuccess)
    }
}
