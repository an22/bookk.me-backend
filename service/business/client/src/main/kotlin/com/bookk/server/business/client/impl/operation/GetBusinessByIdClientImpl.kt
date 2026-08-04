package com.bookk.server.business.client.impl.operation

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.core.client.bodyOrThrow
import com.bookk.server.business.client.impl.BusinessRouting
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import kotlin.uuid.Uuid

internal class GetBusinessByIdClientImpl(
    private val httpClient: HttpClient
) : GetBusinessById {

    override suspend fun invoke(id: Uuid): Result<Business> = runCatching {
        httpClient.get(BusinessRouting.Api.Internal.Business.Id(id = id)).bodyOrThrow()
    }
}
