package com.bookk.server.business.client.api

import kotlin.uuid.Uuid

interface BusinessClient {
    suspend fun getBusinessById(id: Uuid): Result<BusinessDTO>
    suspend fun getPermission(userId: Uuid, businessId: Uuid): Result<Int>
}
