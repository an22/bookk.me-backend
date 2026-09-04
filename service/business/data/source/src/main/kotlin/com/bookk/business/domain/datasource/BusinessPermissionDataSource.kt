package com.bookk.business.domain.datasource

import kotlin.uuid.Uuid

interface BusinessPermissionDataSource {
    suspend fun getPermission(userId: Uuid, businessId: Uuid): Int?
    suspend fun setUserPermissions(userId: Uuid, businessId: Uuid, permission: Int)
    suspend fun deleteUserPermissions(userId: Uuid)
}
