package com.bookk.notifications.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class NotificationSettings(
    val id: Uuid,
    val userId: Uuid,
    val appointmentEnabled: Boolean,
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            appointmentEnabled: Boolean = true,
        ) = NotificationSettings(id, userId, appointmentEnabled)
    }
}
