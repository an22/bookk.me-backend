package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import kotlin.uuid.Uuid

interface EditSettings {
    suspend operator fun invoke(userId: Uuid, settings: AppointmentSettings): Result<Unit>
}