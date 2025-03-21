package com.book.user.domain.api.event

import com.book.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed interface UserEvents : EventStreaming.Event<String> {

    @Serializable
    class DeleteUserEvent(
        val userId: Long,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : UserEvents {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "user.delete_user"
        }
    }
}