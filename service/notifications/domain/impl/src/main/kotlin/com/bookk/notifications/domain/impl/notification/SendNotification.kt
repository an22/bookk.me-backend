package com.bookk.notifications.domain.impl.notification

import com.bookk.notifications.domain.api.entity.CommunicationChannel
import com.bookk.notifications.domain.datasource.NotificationSettingsDataSource
import com.bookk.notifications.domain.impl.channel.NotificationSender
import kotlin.uuid.Uuid

internal class SendNotification(
    val notificationDataSource: NotificationSettingsDataSource,
    val senderMap: Map<CommunicationChannel, NotificationSender>
) {
    suspend operator fun invoke(to: Uuid, notification: NotificationParameters): Result<Unit> = runCatching {
        val settings = notificationDataSource.getByUserId(to) ?: throw IllegalArgumentException("Settings not found")
        val enabledChannelSettings = settings.channels.filter { it.enabled }
        enabledChannelSettings.forEach { settings ->
            senderMap[settings.channel]?.send(to, notification)
        }
    }
}