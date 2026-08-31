package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.datasource.ServiceDataSource
import kotlin.uuid.Uuid

internal suspend fun ServiceDataSource.getServicesExpanded(
    businessId: Uuid,
    serviceIds: List<Uuid>,
    onNotFound: () -> Nothing
): List<Service> {
    val distinctServiceIds = serviceIds.distinct()
    val foundServices = getServicesByIds(distinctServiceIds)
    if (foundServices.size != distinctServiceIds.size || foundServices.any { it.businessId != businessId }) onNotFound()

    val servicesById = foundServices.associateBy { it.id }
    return serviceIds.map { servicesById.getValue(it) }
}
