package com.bookk.server.user.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed interface UserEvent : EventStreaming.Event<String> {
    @Serializable
    data class Updated(
        val userId: Uuid,
        val name: String,
        val lastName: String,
        val email: String,
        val phone: String?,
        val updatedAt: Instant,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : UserEvent {
        override val topic: String = TOPIC
        override val partitionKey: String get() = userId.toString()

        companion object {
            const val TOPIC = "user.updated"
        }
    }
}