package com.book.user.microservice.route

import com.book.user.domain.api.entity.User
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

internal fun Route.createUserDocumentation() {
    install(NotarizedRoute()) {
        tags = setOf("user")
        get = GetInfo.builder {
            security = mapOf(
                "jwt" to emptyList()
            )
            summary("Get current user.")
            description("Get user profile from current authentication token.")
            response {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.OK)
                responseType<User>()
                description("User associated with provided credentials")
            }
            canRespond {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.NotFound)
                responseType<Unit>()
                description("User not found")
            }
        }
    }
}