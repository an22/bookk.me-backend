package com.bookk.notifications.domain.impl

import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.notifications.domain.api.GetNotificationSettings
import com.bookk.notifications.domain.api.entity.NotificationSettings
import com.bookk.notifications.domain.datasource.NotificationTargetDataSource
import com.bookk.notifications.domain.impl.UpdateTargetInformation.Target
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class UpdateTargetInformationTest {

    private class SutFixture {
        val getNotificationSettings = mockk<GetNotificationSettings>()
        val targetDataSource = mockk<NotificationTargetDataSource>()
        val sut = UpdateTargetInformation(getNotificationSettings, targetDataSource)
    }

    @Test
    fun `should upsert email target when target is Email`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val email = "user@example.com"
        with(fixture) {
            coEvery { getNotificationSettings.invoke(userId) } returns Result.success(NotificationSettings.stub(userId = userId))
            coEvery { targetDataSource.upsertEmail(userId, email) } just Runs
        }

        whenn()
        val result = fixture.sut.invoke(userId, Target.Email(email))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.targetDataSource.upsertEmail(userId, email) }
        coVerify(exactly = 0) { fixture.targetDataSource.upsertTelegram(any(), any()) }
    }

    @Test
    fun `should upsert telegram tag when target is Telegram`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        val tag = "@user_tag"
        with(fixture) {
            coEvery { getNotificationSettings.invoke(userId) } returns Result.success(NotificationSettings.stub(userId = userId))
            coEvery { targetDataSource.upsertTelegram(userId, tag) } just Runs
        }

        whenn()
        val result = fixture.sut.invoke(userId, Target.Telegram(tag))

        then()
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fixture.targetDataSource.upsertTelegram(userId, tag) }
        coVerify(exactly = 0) { fixture.targetDataSource.upsertEmail(any(), any()) }
    }

    @Test
    fun `should return failure when getNotificationSettings fails`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            coEvery { getNotificationSettings.invoke(userId) } returns Result.failure(RuntimeException("not found"))
        }

        whenn()
        val result = fixture.sut.invoke(userId, Target.Email("e@example.com"))

        then()
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { fixture.targetDataSource.upsertEmail(any(), any()) }
    }

    @Test
    fun `should return failure when upsertEmail throws`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            coEvery { getNotificationSettings.invoke(userId) } returns Result.success(NotificationSettings.stub(userId = userId))
            coEvery { targetDataSource.upsertEmail(userId, any()) } throws RuntimeException("db error")
        }

        whenn()
        val result = fixture.sut.invoke(userId, Target.Email("e@example.com"))

        then()
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return failure when upsertTelegram throws`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val userId = Uuid.random()
        with(fixture) {
            coEvery { getNotificationSettings.invoke(userId) } returns Result.success(NotificationSettings.stub(userId = userId))
            coEvery { targetDataSource.upsertTelegram(userId, any()) } throws RuntimeException("db error")
        }

        whenn()
        val result = fixture.sut.invoke(userId, Target.Telegram("@tag"))

        then()
        assertTrue(result.isFailure)
    }
}
