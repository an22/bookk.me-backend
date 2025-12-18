package com.bookk.user.microservice.route.api

import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.error.UserErrorCodes
import com.bookk.user.domain.api.operation.GetUserById
import com.bookk.user.microservice.route.UserRouting.Api
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getCurrentUser() {
    authenticate {
        /**
         * Get current user
         * @description Get user profile from current authentication token.
         * @security jwt
         * @tag *user
         * @response 200 application/protobuf [User] User associated with provided credentials
         * @response 404 application/protobuf [SimpleServerError]
         *  Codes:
         *   1. [UserErrorCodes.USER_NOT_EXIST] - User not found
         */
        get<Api.User.Me> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getCurrentUser by application.inject<GetUserById>()

            call.respondWith(getCurrentUser(principal.userId))
        }
    }
}
