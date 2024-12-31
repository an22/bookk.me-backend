package com.bookk.server.user.client.impl.operation

import com.book.user.domain.api.entity.UserExistInfo
import com.book.user.domain.api.entity.UserIdentity
import com.book.user.domain.api.operation.IsUserExistWithParameters
import com.book.user.domain.api.routing.UserRouting
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody

internal class IsUserExistWithParametersClientImpl(
    private val httpClient: HttpClient
) : IsUserExistWithParameters {
    override suspend fun call(params: IsUserExistWithParameters.Param): Result<Boolean> = runCatching {
        httpClient.post(UserRouting.Api.Internal.User.Exist()) {
            setBody(UserIdentity(params.phone, params.email))
        }.body<UserExistInfo>().exists
    }
}