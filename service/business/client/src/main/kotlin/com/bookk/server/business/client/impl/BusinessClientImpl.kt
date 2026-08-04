package com.bookk.server.business.client.impl

import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.api.business.operation.GetBusinessPermission
import com.bookk.server.business.client.api.BusinessClient
import com.bookk.server.business.client.api.BusinessDTO
import kotlin.uuid.Uuid

internal class BusinessClientImpl(
    private val getBusinessById: GetBusinessById,
    private val getBusinessPermission: GetBusinessPermission
) : BusinessClient {
    override suspend fun getBusinessById(id: Uuid): Result<BusinessDTO> {
        return getBusinessById.invoke(id).map(BusinessDTO::from)
    }

    override suspend fun getPermission(userId: Uuid, businessId: Uuid): Result<Int> {
        return getBusinessPermission.invoke(userId, businessId)
    }
}
