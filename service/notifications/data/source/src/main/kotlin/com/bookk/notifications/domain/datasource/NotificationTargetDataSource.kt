package com.bookk.notifications.domain.datasource

import kotlin.uuid.Uuid

interface NotificationTargetDataSource {
    suspend fun getEmail(userId: Uuid): String?
    suspend fun getTelegram(userId: Uuid): String?
    suspend fun upsertEmail(userId: Uuid, email: String)
    suspend fun upsertTelegram(userId: Uuid, telegramTag: String)
}
