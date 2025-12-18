package com.bookk.user.microservice.route.api.internal

import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.enity.respondWith
import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.error.UserErrorCodes
import com.bookk.user.domain.api.operation.GetUserById
import com.bookk.user.microservice.route.UserRouting.Api
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getUserById() {
    /**
     * Get user by id
     * @description Get user profile by email.
     * @tag *internal
     * @response 200 application/protobuf [User] User associated with id
     * @response 404 application/protobuf [SimpleServerError]
     *  Codes:
     *  1. [UserErrorCodes.USER_NOT_EXIST] - User not exist
     */
    get<Api.Internal.User.Id> { user ->
        val getUserById by application.inject<GetUserById>()
        call.respondWith(getUserById(user.id))
    }
}