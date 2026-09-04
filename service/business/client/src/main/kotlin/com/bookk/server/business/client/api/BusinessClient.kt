package com.bookk.server.business.client.api

import com.bookk.business.domain.api.appointment.entity.AppointmentBookingContext
import com.bookk.business.domain.api.business.entity.BusinessResource
import library.permissions.ResourcePermission
import kotlin.uuid.Uuid

interface BusinessClient {
    suspend fun getBusinessById(id: Uuid): Result<BusinessDTO>
    suspend fun getPermission(userId: Uuid, businessId: Uuid, resource: BusinessResource): Result<ResourcePermission>
    suspend fun getAppointmentBookingContext(
        businessId: Uuid,
        employeeId: Uuid,
        userId: Uuid,
        serviceIds: List<Uuid>
    ): Result<AppointmentBookingContext>
}
