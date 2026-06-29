package com.bookk.notifications.domain.impl

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.notifications.domain.api.UpdateNotificationSettings
import com.bookk.notifications.domain.api.entity.NotificationChannelSettings
import com.bookk.notifications.domain.api.entity.NotificationSettings
import com.bookk.notifications.domain.datasource.NotificationSettingsDataSource
import kotlin.uuid.Uuid

internal class UpdateNotificationSettingsImpl(
    private val notificationSettingsDataSource: NotificationSettingsDataSource,
    private val transactionManager: TransactionManager,
) : UpdateNotificationSettings {
    override suspend fun invoke(userId: Uuid, appointmentEnabled: Boolean, channels: List<NotificationChannelSettings>) =
        transactionManager.transaction {
            notificationSettingsDataSource.upsert(
                NotificationSettings(
                    userId = userId,
                    appointmentEnabled = appointmentEnabled,
                    channels = channels
                )
            )
        }
}
