package com.bookk.notifications.data.datasource

import com.bookk.core.data.DataSource
import com.bookk.core.domain.entity.Error
import com.bookk.notifications.data.orm.entity.NotificationChannelsEntity
import com.bookk.notifications.data.orm.entity.NotificationSettingsEntity
import com.bookk.notifications.data.orm.table.NotificationChannelsTable
import com.bookk.notifications.data.orm.table.NotificationSettingsTable
import com.bookk.notifications.domain.api.entity.CommunicationChannel
import com.bookk.notifications.domain.api.entity.NotificationChannelSettings
import com.bookk.notifications.domain.api.entity.NotificationSettings
import com.bookk.notifications.domain.datasource.NotificationSettingsDataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
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

    override suspend fun upsert(settings: NotificationSettings): NotificationSettings = dbQuery {
        val settingsEntity = NotificationSettingsTable.upsertReturning(
            where = { NotificationSettingsTable.userId eq settings.userId.toJavaUuid() },
        ) {
            it[NotificationSettingsTable.userId] = settings.userId.toJavaUuid()
            it[NotificationSettingsTable.appointmentEnabled] = settings.appointmentEnabled
            it[NotificationSettingsTable.updatedAt] = Clock.System.now()
        }
            .map { NotificationSettingsEntity.wrapRow(it) }
            .singleOrNull() ?: throw Error.NotFound()

        NotificationChannelsTable
            .deleteWhere { NotificationChannelsTable.settingsId eq settingsEntity.id }

        val enabledByChannel = settings.channels.associate { it.channel to it.enabled }
        CommunicationChannel.entries
            .map { channel ->
                NotificationChannelSettings(Uuid.random(), channel, enabled = enabledByChannel[channel] ?: false)
            }
            .forEach { channelSettings ->
                NotificationChannelsEntity.new {
                    settingsId = settingsEntity.id
                    channel = channelSettings.channel
                    enabled = channelSettings.enabled
                }
            }

        settingsEntity.domain()
    }

    override suspend fun upsert(channel: NotificationChannelSettings) = dbQuery<Unit> {
        NotificationChannelsEntity.findByIdAndUpdate(channel.id.toJavaUuid()) {
            it.enabled = channel.enabled
        }
    }
}
