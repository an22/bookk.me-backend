package com.bookk.server.business.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

interface BusinessEvent : EventStreaming.Event<String> {

    @Serializable
    data class BusinessDTO(
        val id: Uuid,
        val name: String,
        val address: String
    )

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

    @Serializable
    data class Updated(
        val business: BusinessDTO,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "business.updated"
        }
    }
}