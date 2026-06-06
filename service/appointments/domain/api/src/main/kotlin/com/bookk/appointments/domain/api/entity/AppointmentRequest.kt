package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AppointmentRequest(
    val id: Uuid,
    val userId: Uuid,
    val businessId: Uuid,
    val client: ClientSnapshot,
    val service: ServiceSnapshot,
    val status: AppointmentRequestStatus,
    val date: Instant,
    val note: String,
    val declineReason: String,
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            date: Instant = Instant.fromEpochMilliseconds(0)
        ) = AppointmentRequest(
            id = id,
            userId = userId,
            businessId = businessId,
            client = ClientSnapshot.stub(),
            service = ServiceSnapshot.stub(),
            status = AppointmentRequestStatus.PENDING,
            date = date,
            note = "Note",
            declineReason = ""
        )
    }
}