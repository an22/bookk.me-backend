package com.bookk.server.business.client.api

import com.bookk.business.domain.api.appointment.entity.AppointmentBookingContext
import library.permissions.ObjectPermission
import kotlin.uuid.Uuid

interface BusinessClient {
    suspend fun getBusinessById(id: Uuid): Result<BusinessDTO>
    suspend fun getPermission(userId: Uuid, businessId: Uuid): Result<ObjectPermission>
    suspend fun getAppointmentBookingContext(
        businessId: Uuid,
        employeeId: Uuid,
        clientId: Uuid,
        serviceIds: List<Uuid>
    ): Result<AppointmentBookingContext>
}
