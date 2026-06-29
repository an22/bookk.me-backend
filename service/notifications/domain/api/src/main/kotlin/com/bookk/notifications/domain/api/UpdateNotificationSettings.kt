package com.bookk.notifications.domain.api

import com.bookk.notifications.domain.api.entity.NotificationSettings
import kotlin.uuid.Uuid

interface UpdateNotificationSettings {
    suspend operator fun invoke(userId: Uuid, appointmentEnabled: Boolean): Result<NotificationSettings>
}
