package com.book.user.microservice.route.api

import com.book.core.service.applyMediaType
import com.book.core.service.enity.respondWith
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.entity.UserId
import com.book.user.domain.api.operation.CreateUser
import com.book.user.microservice.route.UserRouting.Api
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postCreateUser() {
    withCreateUserDocumentation()
    post<Api.Internal.User> {
        val user = call.receive<User>()
        val createUser by application.inject<CreateUser>()
        call.respondWith(createUser(user))
    }
}

internal fun Route.withCreateUserDocumentation() {
    install(NotarizedResource<Api.Internal.User>()) {
        tags = setOf("internal", "user")
        post = PostInfo.builder {
            summary("Create new user")
            description("Create new user from provided info")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.Created)
                responseType<UserId>()
                description("User created successfully, id of the new user returned.")
            }
            canRespond {
                applyMediaType()
                responseType<Unit>()
                responseCode(HttpStatusCode.BadRequest)
                description("Error while creating new user")
            }
        }
    }
}