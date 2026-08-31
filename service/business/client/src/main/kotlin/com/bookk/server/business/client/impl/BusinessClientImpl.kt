package com.bookk.server.business.client.impl

import com.bookk.business.domain.api.appointment.entity.AppointmentBookingContext
import com.bookk.business.domain.api.appointment.operation.GetAppointmentBookingContext
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.api.business.operation.GetBusinessPermission
import com.bookk.server.business.client.api.BusinessClient
import com.bookk.server.business.client.api.BusinessDTO
import library.permissions.ObjectPermission
import kotlin.uuid.Uuid

internal class BusinessClientImpl(
    private val getBusinessById: GetBusinessById,
    private val getBusinessPermission: GetBusinessPermission,
    private val getAppointmentBookingContext: GetAppointmentBookingContext
) : BusinessClient {
    override suspend fun getBusinessById(id: Uuid): Result<BusinessDTO> {
        return getBusinessById.invoke(id).map(BusinessDTO::from)
    }

    override suspend fun getPermission(userId: Uuid, businessId: Uuid): Result<ObjectPermission> {
        return getBusinessPermission.invoke(userId, businessId)
    }

    override suspend fun getAppointmentBookingContext(
        businessId: Uuid,
        employeeId: Uuid,
        clientId: Uuid,
        serviceIds: List<Uuid>
    ): Result<AppointmentBookingContext> {
        return getAppointmentBookingContext.invoke(businessId, employeeId, clientId, serviceIds)
    }
}
