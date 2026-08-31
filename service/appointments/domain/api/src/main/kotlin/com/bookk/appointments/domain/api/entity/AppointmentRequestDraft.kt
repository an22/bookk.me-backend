package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AppointmentRequestDraft(
    val businessId: Uuid,
    val employeeId: Uuid,
    val clientId: Uuid,
    val serviceIds: List<Uuid>,
    val date: Instant,
    val note: String,
    val offerToken: String
) {
    companion object {
        fun stub(
            businessId: Uuid = Uuid.random(),
            employeeId: Uuid = Uuid.random(),
            clientId: Uuid = Uuid.random(),
            serviceIds: List<Uuid> = listOf(Uuid.random()),
            date: Instant = Instant.fromEpochMilliseconds(0),
            offerToken: String = "token"
        ) = AppointmentRequestDraft(
            businessId = businessId,
            employeeId = employeeId,
            clientId = clientId,
            serviceIds = serviceIds,
            date = date,
            note = "Note",
            offerToken = offerToken
        )
    }
}
