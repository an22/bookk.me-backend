package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.business.entity.BusinessPermissions
import com.bookk.business.domain.api.business.entity.BusinessResource
import library.permissions.ResourcePermission
import kotlin.uuid.Uuid

interface BusinessPermissionDataSource {
    suspend fun getPermissions(userId: Uuid, businessId: Uuid): BusinessPermissions
    suspend fun getPermission(userId: Uuid, businessId: Uuid, resource: BusinessResource): ResourcePermission
    suspend fun setPermission(userId: Uuid, businessId: Uuid, resource: BusinessResource, permission: ResourcePermission)
    suspend fun deleteUserPermissions(userId: Uuid)
}
