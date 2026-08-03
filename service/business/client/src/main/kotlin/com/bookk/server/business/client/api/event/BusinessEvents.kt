package com.bookk.server.business.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

interface BusinessEvent : EventStreaming.Event<String> {

    @Serializable
    data class BusinessDTO(
        val id: Uuid,
        val name: String,
        val address: String,
        val timeZone: TimeZone
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

    @Serializable
    data class EmployeeInvitationCreated(
        val invitedUserId: Uuid,
        val invitedName: String,
        val businessId: Uuid,
        val businessName: String,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "business.employee_invitation_created"
        }
    }

    @Serializable
    data class EmployeeInvitationApproved(
        val inviterUserId: Uuid,
        val employeeUserId: Uuid,
        val employeeName: String,
        val businessId: Uuid,
        val businessName: String,
        override val idempotencyKey: String = Uuid.random().toString()
    ) : BusinessEvent {
        override val topic: String = TOPIC

        companion object {
            const val TOPIC = "business.employee_invitation_approved"
        }
    }
}