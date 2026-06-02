package com.bookk.appointments.domain.datasource

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import kotlin.uuid.Uuid

interface AppointmentDataSource {
    suspend fun attachBusiness(businessId: Uuid)
    suspend fun initPermissions(userId: Uuid, businessId: Uuid, permissions: Int)
    suspend fun getPermissions(userId: Uuid, businessId: Uuid): Int?

    suspend fun create(request: AppointmentRequest): Appointment
}