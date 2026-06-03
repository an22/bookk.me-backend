package com.bookk.server.auth.client

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

interface AuthEvent : EventStreaming.Event<String> {
    @Serializable
    class UserDeleted(
        val userId: Uuid,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AuthEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "auth.user_deleted"
        }
    }
}