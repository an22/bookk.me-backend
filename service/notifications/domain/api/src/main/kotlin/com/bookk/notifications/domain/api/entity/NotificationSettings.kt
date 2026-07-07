package com.bookk.notifications.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class NotificationSettings(
    val userId: Uuid,
    val appointmentEnabled: Boolean,
    val channels: List<NotificationChannelSettings>,
) {

    constructor(userId: Uuid) : this(
        userId = userId,
        appointmentEnabled = false,
        channels = listOf(
            NotificationChannelSettings(Uuid.random(), CommunicationChannel.PUSH_NOTIFICATIONS, false),
            NotificationChannelSettings(Uuid.random(), CommunicationChannel.EMAIL, false),
            NotificationChannelSettings(Uuid.random(), CommunicationChannel.TELEGRAM, false)
        )
    )

    companion object {
        fun stub(
            userId: Uuid = Uuid.random(),
            appointmentEnabled: Boolean = true,
            channels: List<NotificationChannelSettings> = emptyList(),
        ) = NotificationSettings(userId, appointmentEnabled, channels)
    }
}
