package com.bookk.user.microservice.route.api.internal

import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.enity.respondWith
import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.entity.UserId
import com.bookk.user.domain.api.operation.CreateUser
import com.bookk.user.microservice.route.UserRouting.Api
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postCreateUser() {
    /**
     * Create new user
     * @description Create new user from provided info
     * @security jwt
     * @tag *internal
     * @request application/protobuf [User] User to create, id is ignored
     * @response 201 application/protobuf [UserId] User created successfully, id of the new user returned.
     * @response 400 application/protobuf [SimpleServerError] Error while creating new user
     */
    post<Api.Internal.User> {
        val user = call.receive<User>()
        val createUser by application.inject<CreateUser>()
        call.respondWith(createUser(user))
    }
}