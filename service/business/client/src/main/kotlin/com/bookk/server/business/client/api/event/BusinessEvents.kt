package com.bookk.server.business.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface BusinessEvent : EventStreaming.Event<String> {

    @Serializable
    data class BusinessDTO(
        val id: Uuid,
        val name: String,
        val address: String,
        val timeZone: TimeZone,
        val schedule: ScheduleDTO
    )

    @Serializable
    data class WorkHourDTO(
        val dayOfWeek: DayOfWeek,
        val from: LocalTime,
        val to: LocalTime
    )

    @Serializable
    data class DayOffDTO(
        val start: LocalDate,
        val end: LocalDate
    )

    @Serializable
    data class ScheduleDTO(
        val workingDays: List<DayOfWeek>,
        val workingHours: List<WorkHourDTO>,
        val dayOffs: List<DayOffDTO>
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
        val updatedAt: Instant,
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