package com.bookk.server.business.client.impl.operation

import com.bookk.business.domain.api.client.operation.GetClientBusinessIds
import com.bookk.core.client.bodyOrThrow
import com.bookk.server.business.client.impl.BusinessRouting
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import kotlin.uuid.Uuid

internal class GetClientBusinessIdsClientImpl(
    private val httpClient: HttpClient
) : GetClientBusinessIds {

    override suspend fun invoke(userId: Uuid): Result<List<Uuid>> = runCatching {
        httpClient.get(
            BusinessRouting.Api.Internal.Client.Businesses(
                parent = BusinessRouting.Api.Internal.Client(),
                userId = userId
            )
        ).bodyOrThrow()
    }
}
