package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AppointmentRequestDraft(
    val businessId: Uuid,
    val employeeId: Uuid,
    val services: List<RequestedService>,
    val date: Instant,
    val note: String,
    val offerToken: String
) {
    companion object {
        fun stub(
            businessId: Uuid = Uuid.random(),
            employeeId: Uuid = Uuid.random(),
            services: List<RequestedService> = listOf(RequestedService.stub()),
            date: Instant = Instant.fromEpochMilliseconds(0),
            offerToken: String = "token"
        ) = AppointmentRequestDraft(
            businessId = businessId,
            employeeId = employeeId,
            services = services,
            date = date,
            note = "Note",
            offerToken = offerToken
        )
    }
}
