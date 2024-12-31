package com.book.user.microservice.route.api

import com.book.core.domain.entity.handle
import com.book.core.service.applyMediaType
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.MessageServerError
import com.book.core.service.enity.SimpleServerError
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.routing.UserRouting.Api
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getCurrentUser() {
    withMeDocumentation()
    authenticate {
        get<Api.User.Me> {
            val operation by application.inject<GetUserById>()
            val principal = requireNotNull(call.principal<AppPrincipal>())
            operation.call(principal.userId)
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
}

internal fun Route.withMeDocumentation() {
    install(NotarizedResource<Api.User.Me>()) {
        tags = setOf("user")
        get = GetInfo.builder {
            security = mapOf(
                "jwt" to emptyList()
            )
            summary("Get current user")
            description("Get user profile from current authentication token.")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<User>()
                description("User associated with provided credentials")
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