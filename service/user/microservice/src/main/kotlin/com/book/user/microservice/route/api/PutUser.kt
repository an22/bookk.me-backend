package com.book.user.microservice.route.api

import com.book.core.service.applyMediaType
import com.book.core.service.enity.SimpleServerError
import com.book.core.service.enity.respondWith
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.EditUser
import com.book.user.microservice.route.UserRouting.Api
import io.bkbn.kompendium.core.metadata.PutInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.putUser() {
    withPutUserDocumentation()
    authenticate {
        put<Api.User.Me> {
            val body = call.receive<User>()
            val editUser by application.inject<EditUser>()

            call.respondWith(editUser(body))
        }
    }
}

internal fun Route.withPutUserDocumentation() {
    install(NotarizedResource<Api.User.PutUser>()) {
        tags = setOf("user")
        put = PutInfo.builder {
            security = mapOf("jwt" to emptyList())
            summary("Update user")
            description("Update current user")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("User successfully updated")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.NotFound)
                responseType<SimpleServerError>(ObjectEnrichment(id = "EditUser") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            minimum = 1
                            maximum = 1
                            description = buildString {
                                append("${EditUser.Error.UserNotFound.code} - ${EditUser.Error.UserNotFound.message}\n")
                            }
                        }
                    }
                })
                description("User not found")
            }
        }
    }
}