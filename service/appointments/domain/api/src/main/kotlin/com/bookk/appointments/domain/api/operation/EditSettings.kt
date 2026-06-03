package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.AppointmentSettings

interface EditSettings {
    suspend operator fun invoke(settings: AppointmentSettings)
}