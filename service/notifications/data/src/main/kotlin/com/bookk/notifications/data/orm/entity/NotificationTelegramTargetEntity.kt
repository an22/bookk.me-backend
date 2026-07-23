package com.bookk.notifications.data.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import com.bookk.notifications.data.orm.table.NotificationTelegramTargetTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class NotificationTelegramTargetEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var userId by NotificationTelegramTargetTable.userId
    var telegramTag by NotificationTelegramTargetTable.telegramTag

    companion object : DecoratorUUIDEntityClass<NotificationTelegramTargetEntity>(NotificationTelegramTargetTable)
}
