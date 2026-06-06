package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class AppointmentCancellation(
    val id: Uuid,
    val businessId: Uuid,
    val reason: String
)