package com.bookk.appointments.domain.datasource

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import kotlin.uuid.Uuid

interface AppointmentDataSource {
    suspend fun hasOverlapsWith(request: AppointmentRequest): Boolean
    suspend fun create(request: AppointmentRequest): Appointment
    suspend fun delete(appointment: Appointment)
    suspend fun update(appointment: Appointment): Appointment
    suspend fun getAll(businessId: Uuid): List<Appointment>
}