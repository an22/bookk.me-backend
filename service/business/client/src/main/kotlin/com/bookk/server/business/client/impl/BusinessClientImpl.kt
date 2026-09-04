package com.bookk.server.business.client.impl

import com.bookk.business.domain.api.appointment.entity.AppointmentBookingContext
import com.bookk.business.domain.api.appointment.operation.GetAppointmentBookingContext
import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.api.business.operation.GetBusinessPermission
import com.bookk.server.business.client.api.BusinessClient
import com.bookk.server.business.client.api.BusinessDTO
import library.permissions.ResourcePermission
import kotlin.uuid.Uuid

internal class BusinessClientImpl(
    private val getBusinessById: GetBusinessById,
    private val getBusinessPermission: GetBusinessPermission,
    private val getAppointmentBookingContext: GetAppointmentBookingContext
) : BusinessClient {
    override suspend fun getBusinessById(id: Uuid): Result<BusinessDTO> {
        return getBusinessById.invoke(id).map(BusinessDTO::from)
    }

    override suspend fun getPermission(userId: Uuid, businessId: Uuid, resource: BusinessResource): Result<ResourcePermission> {
        return getBusinessPermission.invoke(userId, businessId, resource)
    }

    override suspend fun getAppointmentBookingContext(
        businessId: Uuid,
        employeeId: Uuid,
        userId: Uuid,
        serviceIds: List<Uuid>
    ): Result<AppointmentBookingContext> {
        return getAppointmentBookingContext.invoke(businessId, employeeId, userId, serviceIds)
    }
}
