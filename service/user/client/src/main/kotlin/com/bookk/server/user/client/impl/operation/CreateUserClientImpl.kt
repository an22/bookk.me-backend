package com.bookk.server.user.client.impl.operation

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.entity.UserId
import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.routing.UserRouting
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody

internal class CreateUserClientImpl(
    private val httpClient: HttpClient
) : CreateUser {

    override suspend fun invoke(user: User) = runCatching {
        httpClient.post(UserRouting.Api.Internal.User()) {
            setBody(user)
        }.body<UserId>().id
    }
}