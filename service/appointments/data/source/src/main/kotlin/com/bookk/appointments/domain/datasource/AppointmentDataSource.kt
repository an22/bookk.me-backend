package com.bookk.appointments.domain.datasource

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest

interface AppointmentDataSource {
    suspend fun hasOverlapsWith(request: AppointmentRequest): Boolean
    suspend fun create(request: AppointmentRequest): Appointment
}