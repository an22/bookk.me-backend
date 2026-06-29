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

internal class UpdateNotificationSettingsImplTest {

    private class SutFixture {
        val notificationSettingsDataSource = mockk<NotificationSettingsDataSource>()
        val transactionManager = mockk<TransactionManager>()
        val sut = UpdateNotificationSettingsImpl(notificationSettingsDataSource, transactionManager)
    }

    @Test
    fun `should update notification settings and return updated settings`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val updatedSettings = NotificationSettings.stub(userId = userId, appointmentEnabled = false)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { notificationSettingsDataSource.upsert(userId, false) } returns updatedSettings
        }

        whenn()
        val result = fixture.sut.invoke(userId, false)

        then()
        assertTrue(result.isSuccess)
        assertEquals(updatedSettings, result.getOrNull())
        coVerify(exactly = 1) { fixture.notificationSettingsDataSource.upsert(userId, false) }
    }

    @Test
    fun `should create notification settings when none exist`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val createdSettings = NotificationSettings.stub(userId = userId, appointmentEnabled = true)
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { notificationSettingsDataSource.upsert(userId, true) } returns createdSettings
        }

        whenn()
        val result = fixture.sut.invoke(userId, true)

        then()
        assertTrue(result.isSuccess)
        assertEquals(createdSettings, result.getOrNull())
        coVerify(exactly = 1) { fixture.notificationSettingsDataSource.upsert(userId, true) }
    }

    @Test
    fun `should return failure when datasource throws`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            transactionManager.mockTransaction()
            coEvery { notificationSettingsDataSource.upsert(userId, any()) } throws RuntimeException("db error")
        }

        whenn()
        val result = fixture.sut.invoke(userId, true)

        then()
        assertTrue(result.isFailure)
    }
}
