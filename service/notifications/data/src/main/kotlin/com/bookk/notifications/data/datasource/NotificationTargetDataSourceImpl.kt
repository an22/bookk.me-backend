package com.bookk.notifications.data.datasource

import com.bookk.core.data.DataSource
import com.bookk.notifications.data.orm.table.NotificationEmailTargetTable
import com.bookk.notifications.data.orm.table.NotificationTelegramTargetTable
import com.bookk.notifications.domain.datasource.NotificationTargetDataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class NotificationTargetDataSourceImpl : DataSource(), NotificationTargetDataSource {

    override suspend fun getEmail(userId: Uuid): String? {
        return NotificationEmailTargetTable.select(NotificationEmailTargetTable.email)
            .where { NotificationEmailTargetTable.userId eq userId.toJavaUuid() }
            .singleOrNull()
            ?.get(NotificationEmailTargetTable.email)
    }

    override suspend fun getTelegram(userId: Uuid): String? {
        return NotificationTelegramTargetTable.select(NotificationTelegramTargetTable.telegramTag)
            .where { NotificationTelegramTargetTable.userId eq userId.toJavaUuid() }
            .singleOrNull()
            ?.get(NotificationTelegramTargetTable.telegramTag)
    }

    override suspend fun upsertEmail(userId: Uuid, email: String) = dbQuery<Unit> {
        NotificationEmailTargetTable.upsert(
            where = { NotificationEmailTargetTable.userId eq userId.toJavaUuid() }
        ) {
            it[NotificationEmailTargetTable.userId] = userId.toJavaUuid()
            it[NotificationEmailTargetTable.email] = email
        }
    }

    override suspend fun upsertTelegram(userId: Uuid, telegramTag: String) = dbQuery<Unit> {
        NotificationTelegramTargetTable.upsert(
            where = { NotificationTelegramTargetTable.userId eq userId.toJavaUuid() }
        ) {
            it[NotificationTelegramTargetTable.userId] = userId.toJavaUuid()
            it[NotificationTelegramTargetTable.telegramTag] = telegramTag
        }
    }
}
