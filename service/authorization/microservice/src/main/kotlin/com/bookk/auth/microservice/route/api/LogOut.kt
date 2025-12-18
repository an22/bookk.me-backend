package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.signout.operation.SignOut
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.delete
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.logOut() {
    /**
     * Sign Out
     * @description Mark user as signed out
     * @tag *auth
     * @security jwt
     * @response 200 Success
     */
    authenticate {
        delete<Api.Auth.SignOut> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val signOut by application.inject<SignOut>()
            call.respondWith(signOut(principal.deviceId))
        }
    }
}
