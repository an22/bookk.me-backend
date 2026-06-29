package com.bookk.notifications.data.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import com.bookk.notifications.data.orm.table.NotificationSettingsTable
import com.bookk.notifications.domain.api.entity.NotificationSettings
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.uuid.toKotlinUuid

internal class NotificationSettingsEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var userId by NotificationSettingsTable.userId
    var appointmentEnabled by NotificationSettingsTable.appointmentEnabled

    fun domain() = NotificationSettings(
        id = id.value.toKotlinUuid(),
        userId = userId.toKotlinUuid(),
        appointmentEnabled = appointmentEnabled,
    )

    companion object : DecoratorUUIDEntityClass<NotificationSettingsEntity>(NotificationSettingsTable)
}
