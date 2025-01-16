package com.bookk.server.user.client.impl.operation

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.bookk.server.user.client.impl.UserRouting
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get

internal class GetUserByIdClientImpl(
    private val httpClient: HttpClient
) : GetUserById {

    override suspend fun invoke(userId: Long): Result<User> = runCatching {
        httpClient.get(UserRouting.Api.Internal.User.Id(id = userId)).body()
    }

}