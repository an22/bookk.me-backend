package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}