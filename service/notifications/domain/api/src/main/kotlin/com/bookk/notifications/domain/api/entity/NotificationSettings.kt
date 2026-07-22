package com.bookk.notifications.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class NotificationSettings(
    val id: Uuid,
    val userId: Uuid,
    val appointmentEnabled: Boolean,
    val channels: List<NotificationChannelSettings>,
) {

    @Serializable
    data class Update(
        val id: Uuid,
        val appointmentEnabled: Boolean,
        val channels: List<NotificationChannelSettings>,
    )

    constructor(userId: Uuid) : this(
        id = Uuid.random(),
        userId = userId,
        appointmentEnabled = false,
        channels = listOf(
            NotificationChannelSettings(Uuid.random(), CommunicationChannel.PUSH_NOTIFICATIONS, false, true),
            NotificationChannelSettings(Uuid.random(), CommunicationChannel.EMAIL, false, true),
            NotificationChannelSettings(Uuid.random(), CommunicationChannel.TELEGRAM, false, true)
        )
    )

    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            appointmentEnabled: Boolean = true,
            channels: List<NotificationChannelSettings> = emptyList(),
        ) = NotificationSettings(id, userId, appointmentEnabled, channels)
    }
}
