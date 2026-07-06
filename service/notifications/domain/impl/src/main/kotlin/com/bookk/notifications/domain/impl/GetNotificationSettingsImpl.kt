package com.bookk.notifications.domain.impl

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.notifications.domain.api.GetNotificationSettings
import com.bookk.notifications.domain.api.entity.CommunicationChannel
import com.bookk.notifications.domain.api.entity.NotificationChannelSettings
import com.bookk.notifications.domain.api.entity.NotificationSettings
import com.bookk.notifications.domain.datasource.NotificationSettingsDataSource
import kotlin.uuid.Uuid

internal class GetNotificationSettingsImpl(
    private val notificationSettingsDataSource: NotificationSettingsDataSource,
    private val transactionManager: TransactionManager,
) : GetNotificationSettings {
    override suspend fun invoke(userId: Uuid) = transactionManager.transaction {
        notificationSettingsDataSource.getByUserId(userId)
            ?: notificationSettingsDataSource.upsert(
                NotificationSettings(
                    userId = userId,
                    appointmentEnabled = true,
                    channels = listOf(
                        NotificationChannelSettings(Uuid.random(), CommunicationChannel.PUSH_NOTIFICATIONS, false),
                        NotificationChannelSettings(Uuid.random(), CommunicationChannel.EMAIL, false),
                        NotificationChannelSettings(Uuid.random(), CommunicationChannel.TELEGRAM, false)
                    )
                )
            )
    }
}
