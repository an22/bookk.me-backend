package com.bookk.notifications.data.datasource

import com.bookk.core.data.DataSource
import com.bookk.core.domain.entity.Error
import com.bookk.notifications.data.orm.entity.NotificationSettingsEntity
import com.bookk.notifications.data.orm.table.NotificationSettingsTable
import com.bookk.notifications.domain.api.entity.NotificationSettings
import com.bookk.notifications.domain.datasource.NotificationSettingsDataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class NotificationSettingsDataSourceImpl : DataSource(), NotificationSettingsDataSource {

    override suspend fun getByUserId(userId: Uuid): NotificationSettings? = dbQuery {
        NotificationSettingsEntity
            .find { NotificationSettingsTable.userId eq userId.toJavaUuid() }
            .firstOrNull()
            ?.domain()
    }

    override suspend fun upsert(userId: Uuid, appointmentEnabled: Boolean): NotificationSettings = dbQuery {
        NotificationSettingsTable.upsertReturning(
            where = { NotificationSettingsTable.userId eq userId.toJavaUuid() },
        ) {
            it[NotificationSettingsTable.userId] = userId.toJavaUuid()
            it[NotificationSettingsTable.appointmentEnabled] = appointmentEnabled
            it[NotificationSettingsTable.updatedAt] = Clock.System.now()
        }
            .map { NotificationSettingsEntity.wrapRow(it).domain() }
            .singleOrNull() ?: throw Error.NotFound()
    }
}
