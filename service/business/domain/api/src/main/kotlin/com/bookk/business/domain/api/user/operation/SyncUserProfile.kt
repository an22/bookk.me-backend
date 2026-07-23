package com.bookk.business.domain.api.user.operation

import kotlin.uuid.Uuid

interface SyncUserProfile {
    suspend operator fun invoke(
        userId: Uuid,
        name: String,
        lastName: String,
        phone: String,
        email: String
    ): Result<Unit>
}
