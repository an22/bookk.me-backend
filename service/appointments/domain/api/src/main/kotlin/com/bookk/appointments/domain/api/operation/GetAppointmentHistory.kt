package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.core.domain.entity.Pagination
import kotlin.uuid.Uuid

interface GetAppointmentHistory {
    suspend operator fun invoke(
        userId: Uuid,
        businessId: Uuid,
        limit: Int,
        offset: Long
    ): Result<Pagination<Appointment>>
}