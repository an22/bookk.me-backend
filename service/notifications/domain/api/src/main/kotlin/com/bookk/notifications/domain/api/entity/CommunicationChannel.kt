package com.bookk.notifications.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
enum class CommunicationChannel {
    TELEGRAM,
    EMAIL,
    PUSH_NOTIFICATIONS,
}
