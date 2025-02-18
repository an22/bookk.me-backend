package com.bookk.server.user.client.impl.operation

import com.book.user.domain.api.entity.UserEditModel
import com.book.user.domain.api.operation.EditUser
import com.bookk.server.user.client.impl.UserRouting
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.patch
import io.ktor.client.request.setBody

internal class EditUserClientImpl(
    private val httpClient: HttpClient
) : EditUser {
    override suspend fun invoke(id: Long, user: UserEditModel): Result<Unit> = runCatching {
        httpClient.patch(UserRouting.Api.Internal.User.Edit(id = id)) {
            setBody(user)
        }
    }
}