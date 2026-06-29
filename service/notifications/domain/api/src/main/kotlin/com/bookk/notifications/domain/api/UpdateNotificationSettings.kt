package com.bookk.notifications.domain.api

import com.bookk.notifications.domain.api.entity.NotificationChannelSettings
import com.bookk.notifications.domain.api.entity.NotificationSettings
import kotlin.uuid.Uuid

interface UpdateNotificationSettings {
    suspend operator fun invoke(
        userId: Uuid,
        appointmentEnabled: Boolean,
        channels: List<NotificationChannelSettings>,
    ): Result<NotificationSettings>
}
