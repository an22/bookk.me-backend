package com.bookk.notifications.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class NotificationChannelSettings(
    val id: Uuid,
    val channel: CommunicationChannel,
    val enabled: Boolean,
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            channel: CommunicationChannel = CommunicationChannel.EMAIL,
            enabled: Boolean = false,
        ) = NotificationChannelSettings(id, channel, enabled)
    }
}
