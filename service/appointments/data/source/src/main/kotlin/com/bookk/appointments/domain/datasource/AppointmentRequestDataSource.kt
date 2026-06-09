package com.bookk.appointments.domain.datasource

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import kotlin.uuid.Uuid

interface AppointmentRequestDataSource {

    suspend fun get(id: Uuid): AppointmentRequest?
    suspend fun getAll(businessId: Uuid): List<AppointmentRequest>
    suspend fun create(request: AppointmentRequest): AppointmentRequest
    suspend fun update(request: AppointmentRequest): AppointmentRequest
    suspend fun delete(request: AppointmentRequest)
    suspend fun approve(request: AppointmentRequest)
    suspend fun decline(id: Uuid, reason: String): AppointmentRequest

    suspend fun hasOverlapsWith(request: AppointmentRequest): Boolean
}