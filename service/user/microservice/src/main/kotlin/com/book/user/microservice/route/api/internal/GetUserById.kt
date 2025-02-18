package com.book.user.microservice.route.api.internal

import com.book.core.service.applyMediaType
import com.book.core.service.enity.respondWith
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.microservice.route.UserRouting.Api
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getUserById() {
    withUserIdDocumentation()
    get<Api.Internal.User.Id> { user ->
        val getUserById by application.inject<GetUserById>()
        call.respondWith(getUserById(user.id))
    }
}

internal fun Route.withUserIdDocumentation() {
    install(NotarizedResource<Api.Internal.User.Id>()) {
        tags = setOf("internal")
        get = GetInfo.builder {
            summary("Get user by id")
            description("Get user profile by id.")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<User>()
                description("User associated with id")
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