package com.bookk.business.domain.api.operation

import com.bookk.business.domain.api.entity.ClientRemote
import kotlin.uuid.Uuid

interface GetClients {
    suspend operator fun invoke(businessId: Uuid): Result<List<ClientRemote>>
}