package com.bookk.notifications.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable
import com.bookk.notifications.domain.api.entity.CommunicationChannel

object NotificationChannelsTable : BaseUUIDTable("notification_channels") {
    val settingsId = reference("settings_id", NotificationSettingsTable)
    val channel = enumeration("channel", CommunicationChannel::class)
    val enabled = bool("enabled")

    init {
        uniqueIndex(settingsId, channel)
    }
}
