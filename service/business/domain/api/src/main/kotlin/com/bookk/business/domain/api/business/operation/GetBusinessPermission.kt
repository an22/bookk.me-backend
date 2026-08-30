package com.bookk.business.domain.api.business.operation

import library.permissions.ObjectPermission
import kotlin.uuid.Uuid

interface GetBusinessPermission {
    suspend operator fun invoke(userId: Uuid, businessId: Uuid): Result<ObjectPermission>
}
