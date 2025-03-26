package com.book.auth.microservice.route.api

import com.book.auth.domain.api.signout.operation.SignOut
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.delete
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.logOut() {
    withSignOutDocumentation()
    authenticate {
        delete<Api.Auth.SignOut> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val signOut by application.inject<SignOut>()
            call.respondWith(signOut(principal.deviceId))
        }
    }
}

internal fun Route.withSignOutDocumentation() {
    install(NotarizedResource<Api.Auth.SignOut>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        delete = DeleteInfo.builder {
            summary("Sign Out")
            description("Mark user as signed out")
            response {
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("Success operation")
            }
        }
    }
}
