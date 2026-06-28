package com.bookk.notifications.data.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import com.bookk.notifications.data.orm.table.DeviceTable
import com.bookk.notifications.domain.api.entity.Device
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.uuid.toKotlinUuid

internal class DeviceEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var authId by DeviceTable.authId
    var deviceUUID by DeviceTable.deviceUuid
    var userId by DeviceTable.userId
    var notificationToken by DeviceTable.notificationToken

    fun domain() = Device(
        id = id.value.toKotlinUuid(),
        authId = authId.toKotlinUuid(),
        deviceUuid = deviceUUID.toKotlinUuid(),
        userId = userId.toKotlinUuid(),
        notificationToken = notificationToken,
    )

    companion object : DecoratorUUIDEntityClass<DeviceEntity>(DeviceTable)
}
