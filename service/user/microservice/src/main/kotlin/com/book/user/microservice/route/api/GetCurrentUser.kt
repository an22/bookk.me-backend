package com.book.user.microservice.route.api

import com.book.core.service.applyMediaType
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.respondWith
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.microservice.route.UserRouting.Api
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getCurrentUser() {
    withMeDocumentation()
    authenticate {
        get<Api.User.Me> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getCurrentUser by application.inject<GetUserById>()

            call.respondWith(getCurrentUser(principal.userId))
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