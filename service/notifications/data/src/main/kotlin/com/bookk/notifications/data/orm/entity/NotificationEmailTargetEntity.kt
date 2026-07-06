package com.bookk.notifications.data.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import com.bookk.notifications.data.orm.table.NotificationEmailTargetTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class NotificationEmailTargetEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var userId by NotificationEmailTargetTable.userId
    var email by NotificationEmailTargetTable.email

    companion object : DecoratorUUIDEntityClass<NotificationEmailTargetEntity>(NotificationEmailTargetTable)
}
