package com.bookk.notifications.domain.impl

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.datasource.transaction.mockTransaction
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.notifications.domain.api.entity.NotificationSettings
import com.bookk.notifications.domain.datasource.NotificationSettingsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class GetNotificationSettingsImplTest {

    private class SutFixture {
        val notificationSettingsDataSource = mockk<NotificationSettingsDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = GetNotificationSettingsImpl(notificationSettingsDataSource, transactionManager)
    }

    @Test
    fun `should return existing notification settings for user`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val settings = NotificationSettings.stub(userId = userId)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { notificationSettingsDataSource.getByUserId(userId) } returns settings
        }

        whenn()
        val result = fixture.sut.invoke(userId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(settings, result.getOrNull())
        coVerify(exactly = 0) { fixture.notificationSettingsDataSource.upsert(any(), any()) }
    }

    @Test
    fun `should create default settings when none exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val defaultSettings = NotificationSettings.stub(userId = userId, appointmentEnabled = true)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { notificationSettingsDataSource.getByUserId(userId) } returns null
            coEvery { notificationSettingsDataSource.upsert(userId, appointmentEnabled = true) } returns defaultSettings
        }

        whenn()
        val result = fixture.sut.invoke(userId)

        then()
        assertTrue(result.isSuccess)
        assertEquals(defaultSettings, result.getOrNull())
        coVerify(exactly = 1) { fixture.notificationSettingsDataSource.upsert(userId, appointmentEnabled = true) }
    }
}
