package com.bookk.server.user.client.api.event

import com.book.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

sealed interface UserEvents : EventStreaming.Event<String> {

    @Serializable
    class DeleteUserEvent(
        val userId: Uuid,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : UserEvents {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "user.delete_user"
        }
    }
}