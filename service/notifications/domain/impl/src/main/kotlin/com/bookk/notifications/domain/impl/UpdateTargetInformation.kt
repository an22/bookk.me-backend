package com.bookk.notifications.domain.impl

import com.bookk.notifications.domain.api.GetNotificationSettings
import com.bookk.notifications.domain.datasource.NotificationTargetDataSource
import kotlin.uuid.Uuid

internal class UpdateTargetInformation(
    private val getNotificationSettings: GetNotificationSettings,
    private val targetDataSource: NotificationTargetDataSource
) {
    suspend operator fun invoke(userId: Uuid, target: Target): Result<Unit> = runCatching {
        getNotificationSettings.invoke(userId).getOrThrow()

        when (target) {
            is Target.Email -> {
                targetDataSource.upsertEmail(userId, target.newEmail)
            }
            is Target.Telegram -> {
                targetDataSource.upsertTelegram(userId, target.newTag)
            }
        }
    }


    internal sealed interface Target {
        class Email(val newEmail: String) : Target
        class Telegram(val newTag: String) : Target
    }
}