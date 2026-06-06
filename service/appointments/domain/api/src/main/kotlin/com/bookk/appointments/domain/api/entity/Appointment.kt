package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Appointment(
    val id: Uuid,
    val userId: Uuid,
    val businessId: Uuid,
    val client: ClientSnapshot,
    val service: ServiceSnapshot,
    val status: AppointmentStatus,
    val date: Instant,
    val note: String,
    val cancellationReason: String
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            date: Instant = Instant.fromEpochMilliseconds(0)
        ) = Appointment(
            id = id,
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot.stub(),
            service = ServiceSnapshot.stub(),
            status = AppointmentStatus.SCHEDULED,
            date = date,
            note = "Note",
            cancellationReason = ""
        )
    }
}