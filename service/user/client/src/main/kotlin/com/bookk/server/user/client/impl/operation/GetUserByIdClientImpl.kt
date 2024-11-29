package com.bookk.server.user.client.impl.operation

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.routing.UserRouting
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get

internal class GetUserByIdClientImpl(
    private val httpClient: HttpClient
) : GetUserById {
    override suspend fun call(params: Long): Result<User> = runCatching {
        httpClient.get(UserRouting.Api.Internal.User.Id(id = params)).body()
    }
}