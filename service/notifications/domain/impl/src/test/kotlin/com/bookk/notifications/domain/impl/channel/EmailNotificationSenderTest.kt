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

internal class EmailNotificationSenderTest {

    private class SutFixture {
        val transactionManager = mockk<TransactionManager>()
        val targetDataSource = mockk<NotificationTargetDataSource>()
        val sut = EmailNotificationSender(transactionManager, targetDataSource)
    }

    private fun notificationParams() = NotificationParameters(
        type = NotificationType.APPOINTMENT,
        push = PushNotification(title = "Title", subtitle = "Subtitle"),
        email = EmailNotification(subject = "Subject", body = "Body"),
        text = TextNotification(text = "Text"),
    )

    @Test
    fun `should fetch email target and return success`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { targetDataSource.getEmail(userId) } returns "user@example.com"
        }

        whenn()
        val result = fixture.sut.send(userId, notificationParams())

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.targetDataSource.getEmail(userId) }
    }

    @Test
    fun `should return success when email target is not configured`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { targetDataSource.getEmail(userId) } returns null
        }

        whenn()
        val result = fixture.sut.send(userId, notificationParams())

        then()
        assertTrue(result.isSuccess)
    }
}
