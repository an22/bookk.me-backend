package com.bookk.notifications.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
data class NotificationChannelSettings(
    val channel: CommunicationChannel,
    val enabled: Boolean,
)
