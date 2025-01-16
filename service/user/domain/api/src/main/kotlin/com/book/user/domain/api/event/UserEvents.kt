package com.book.user.domain.api.event

import com.book.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable

sealed interface UserEvents : EventStreaming.Event<String> {
    @Serializable
    class DeleteUserEvent(
        val userId: Long
    ) : UserEvents {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "user.delete_user"
        }
    }
}