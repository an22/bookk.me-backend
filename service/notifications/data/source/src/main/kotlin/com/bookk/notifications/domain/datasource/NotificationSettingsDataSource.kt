package com.bookk.notifications.domain.datasource

import com.bookk.notifications.domain.api.entity.NotificationSettings
import kotlin.uuid.Uuid

interface NotificationSettingsDataSource {
    suspend fun getByUserId(userId: Uuid): NotificationSettings?
    suspend fun upsert(userId: Uuid, appointmentEnabled: Boolean): NotificationSettings
}
