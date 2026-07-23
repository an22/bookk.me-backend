package com.bookk.notifications.data.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import com.bookk.notifications.data.orm.table.NotificationChannelsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class NotificationChannelsEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var settingsId by NotificationChannelsTable.settingsId
    var channel by NotificationChannelsTable.channel
    var enabled by NotificationChannelsTable.enabled
    var availableToClients by NotificationChannelsTable.availableToClients

    companion object : DecoratorUUIDEntityClass<NotificationChannelsEntity>(NotificationChannelsTable)
}
