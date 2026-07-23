package com.bookk.server.auth.client

import com.bookk.core.data.eventstreaming.EventStreaming
import com.bookk.core.domain.entity.Language
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

interface AuthEvent : EventStreaming.Event<String> {
    @Serializable
    data class UserDeleted(
        val userId: Uuid,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AuthEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "auth.user_deleted"
        }
    }

    @Serializable
    data class DeviceCreated(
        val authId: Uuid,
        val userId: Uuid,
        val deviceUuid: Uuid,
        val language: Language,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AuthEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "auth.device_created"
        }
    }

    @Serializable
    data class DeviceLanguageUpdated(
        val deviceUuid: Uuid,
        val language: Language,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AuthEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "auth.device_language_updated"
        }
    }

    @Serializable
    data class DeviceDeleted(
        val deviceUuid: Uuid,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AuthEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "auth.device_deleted"
        }
    }
}