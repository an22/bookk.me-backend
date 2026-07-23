package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.signout.operation.SignOut
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.service.enity.respondWith
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.delete
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.logOut() {
    authenticate {
        /**
         * Summary: Sign Out
         * Description: Mark user as signed out
         * Tag: auth
         * Security: jwt
         * Response: 200 application/x-protobuf Success
         */
        delete<Api.Auth.SignOut> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val signOut by application.inject<SignOut>()
            call.respondWith(signOut(principal.deviceId))
        }
    }
}
