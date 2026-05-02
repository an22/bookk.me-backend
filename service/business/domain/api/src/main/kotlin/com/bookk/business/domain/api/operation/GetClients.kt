package com.bookk.business.domain.api.operation

import com.bookk.business.domain.api.entity.Client
import kotlin.uuid.Uuid

interface GetClients {
    suspend operator fun invoke(businessId: Uuid): Result<List<Client>>
}