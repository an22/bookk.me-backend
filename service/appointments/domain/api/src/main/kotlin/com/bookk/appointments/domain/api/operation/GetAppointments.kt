package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.Appointment
import kotlin.uuid.Uuid

interface GetAppointments {
    suspend operator fun invoke(userId:Uuid, businessId: Uuid): Result<List<Appointment>>
}