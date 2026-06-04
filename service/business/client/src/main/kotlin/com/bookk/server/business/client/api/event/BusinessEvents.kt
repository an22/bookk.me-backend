package com.bookk.server.business.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

interface BusinessEvent : EventStreaming.Event<String> {
    @Serializable
    data class Deleted(
        val businessId: Uuid,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "business.deleted"
        }
    }
}