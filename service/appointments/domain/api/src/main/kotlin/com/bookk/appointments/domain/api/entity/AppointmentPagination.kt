package com.bookk.appointments.domain.api.entity

import com.bookk.core.domain.entity.PaginationMetadata
import kotlinx.serialization.Serializable

@Serializable
class AppointmentPagination(
    val data: List<Appointment>,
    val metadata: PaginationMetadata
)