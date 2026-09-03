package com.bookk.server.business.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming
import com.bookk.server.business.client.api.BusinessDTO
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface BusinessEvent : EventStreaming.Event<String> {

    @Serializable
    data class Deleted(
        val businessId: Uuid,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC
        override val partitionKey: String get() = businessId.toString()

        companion object {
            const val TOPIC = "business.deleted"
        }
    }

    @Serializable
    data class Updated(
        val business: BusinessDTO,
        val updatedAt: Instant,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC
        override val partitionKey: String get() = business.id.toString()

        companion object {
            const val TOPIC = "business.updated"
        }
    }

    @Serializable
    data class EmployeeInvitationRedeemed(
        val inviterUserId: Uuid,
        val employeeUserId: Uuid,
        val employeeName: String,
        val businessId: Uuid,
        val businessName: String,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "business.employee_invitation_redeemed"
        }
    }

    @Serializable
    data class EmployeePermissionChanged(
        val employeeUserId: Uuid,
        val businessId: Uuid,
        val permission: Int,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC
        override val partitionKey: String get() = businessId.toString()

        companion object {
            const val TOPIC = "business.employee_permission_changed"
        }
    }
}