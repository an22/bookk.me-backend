package com.bookk.server.business.client.api.event

import com.book.core.data.eventstreaming.EventStreaming
import com.bookk.core.newRandomUUIDString
import kotlinx.serialization.Serializable

interface BusinessEvent : EventStreaming.Event<String> {
    @Serializable
    class DeleteBusinessesForUserEvent(
        val userId: Long,
        override val idempotencyKey: String = newRandomUUIDString()
    ) : BusinessEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "business.delete_for_user"
        }
    }
}