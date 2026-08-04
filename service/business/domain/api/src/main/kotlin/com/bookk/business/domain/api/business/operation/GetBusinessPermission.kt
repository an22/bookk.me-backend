package com.bookk.business.domain.api.business.operation

import kotlin.uuid.Uuid

interface GetBusinessPermission {
    suspend operator fun invoke(userId: Uuid, businessId: Uuid): Result<Int>

    companion object {
        const val NO_PERMISSION = 0
    }
}
