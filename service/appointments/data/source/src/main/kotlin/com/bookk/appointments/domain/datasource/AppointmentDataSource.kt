package com.bookk.appointments.domain.datasource

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRepresentation
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.core.domain.entity.Pagination
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface AppointmentDataSource {
    suspend fun get(id: Uuid): Appointment
    suspend fun hasOverlapsWith(appointment: AppointmentRepresentation): Boolean
    suspend fun create(request: AppointmentRequest): Appointment
    suspend fun create(appointment: Appointment): Appointment
    suspend fun delete(id: Uuid)
    suspend fun cancel(id: Uuid, reason: String): Appointment
    suspend fun update(appointment: Appointment): Appointment
    suspend fun getAll(businessId: Uuid): List<Appointment>
    suspend fun getAllForDate(businessId: Uuid, range: ClosedRange<Instant>): List<Appointment>
    suspend fun getAllPaginated(businessId: Uuid, limit: Int, offset: Long): Pagination<Appointment>
}