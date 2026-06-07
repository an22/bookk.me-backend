package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AppointmentCancellation(
    val id: Uuid,
    val businessId: Uuid,
    val reason: String
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
        ) = AppointmentCancellation(
            id = id,
            businessId = businessId,
            reason = "Test reason"
        )
    }
}