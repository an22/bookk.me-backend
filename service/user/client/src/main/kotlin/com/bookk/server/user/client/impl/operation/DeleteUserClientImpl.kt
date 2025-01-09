package com.bookk.server.user.client.impl.operation

import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.routing.UserRouting
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get

internal class DeleteUserClientImpl(
    private val httpClient: HttpClient
) : DeleteUser {

    override suspend fun invoke(userId: Long) : Result<Unit> = runCatching {
        httpClient.get(UserRouting.Api.Internal.User.Id(id = userId))
    }

}