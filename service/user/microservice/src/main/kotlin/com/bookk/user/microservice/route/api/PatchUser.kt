package com.bookk.user.microservice.route.api

import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import com.bookk.user.domain.api.entity.UserEditModel
import com.bookk.user.domain.api.error.UserErrorCodes
import com.bookk.user.domain.api.operation.EditUser
import com.bookk.user.microservice.route.UserRouting.Api
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.patch
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.patchUser() {
    authenticate {
        /**
         * Update user
         * @description Update current user from authentication token.
         * @security jwt
         * @tag *user
         * @request application/protobuf [UserEditModel] Fields that needs to be updated
         * @response 200 User successfully updated
         * @response 404 application/protobuf [SimpleServerError]
         *  Codes:
         *   1. [UserErrorCodes.USER_NOT_EXIST] - User not found
         */
        patch<Api.User.Me> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<UserEditModel>()
            val editUser by application.inject<EditUser>()

            call.respondWith(editUser(principal.userId, body))
        }
    }
}