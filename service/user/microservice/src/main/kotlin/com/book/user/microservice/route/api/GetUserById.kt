package com.book.user.microservice.route.api

import com.book.core.domain.entity.handle
import com.book.core.service.applyMediaType
import com.book.core.service.enity.MessageServerError
import com.book.core.service.enity.SimpleServerError
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.routing.UserRouting.Api
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getUserById() {
    withUserIdDocumentation()
    get<Api.Internal.User.Id> { user ->
        val operation by application.inject<GetUserById>()
        operation.call(user.id)
            .handle<_, GetUserById.GetCurrentUserError>(
                onSuccess = {
                    call.respond(it)
                },
                onBusinessError = {
                    when (it) {
                        GetUserById.GetCurrentUserError.UserNotFound -> {
                            call.respond(
                                HttpStatusCode.NotFound,
                                SimpleServerError(it.message.orEmpty(), it.code)
                            )
                        }
                    }
                },
                onUnexpectedError = {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MessageServerError(it.message.orEmpty())
                    )
                }
            )
    }
}

internal fun Route.withUserIdDocumentation() {
    install(NotarizedResource<Api.Internal.User.Id>()) {
        tags = setOf("internal", "user")
        get = GetInfo.builder {
            summary("Get user by id")
            description("Get user profile by id.")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<User>()
                description("User associated with id")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.NotFound)
                responseType<Unit>()
                description("User not found")
            }
        }
    }
}