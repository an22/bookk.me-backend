package com.bookk.business.domain.api.service.operation

import kotlin.uuid.Uuid

interface DeleteService {
    suspend operator fun invoke(id: Uuid): Result<Unit>
}