package com.bookk.business.domain.api.operation

import kotlin.uuid.Uuid

interface DeleteBusiness {
    suspend operator fun invoke(userId: Uuid): Result<Unit>
}