package com.bookk.notifications.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Device(
    val id: Uuid,
    val authId: Uuid,
    val deviceUuid: Uuid,
    val userId: Uuid,
    val notificationToken: String?,
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            authId: Uuid = Uuid.random(),
            deviceId: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            notificationToken: String? = null,
        ) = Device(id, authId, deviceId, userId, notificationToken)
    }
}
