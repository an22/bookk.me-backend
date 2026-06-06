package com.bookk.server.appointments.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface AppointmentEvent : EventStreaming.Event<String> {

    @Serializable
    data class RequestCreated(
        val from: Instant,
        val to: Instant,
        val businessName: String,
        val address: String,
        val executioner: String,
        val price: String,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AppointmentEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "appointment.request_approved"
        }
    }

    @Serializable
    data class RequestApproved(
        val from: Instant,
        val to: Instant,
        val businessName: String,
        val address: String,
        val executioner: String,
        val price: String,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AppointmentEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "appointment.request_approved"
        }
    }

    @Serializable
    data class RequestRejected(
        val from: Instant,
        val to: Instant,
        val address: String,
        val businessName: String,
        val executioner: String,
        val price: String,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AppointmentEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "appointment.request_rejected"
        }
    }

    @Serializable
    data class Cancelled(
        val from: Instant,
        val to: Instant,
        val address: String,
        val businessName: String,
        val executioner: String,
        val price: String,
        val reason: String,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : AppointmentEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "appointment.cancelled"
        }
    }
}