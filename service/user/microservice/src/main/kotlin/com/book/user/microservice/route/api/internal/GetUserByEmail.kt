package com.book.user.microservice.route.api.internal

import com.book.core.domain.entity.SimpleServerError
import com.book.core.service.applyMediaType
import com.book.core.service.enity.respondWith
import com.book.user.domain.api.entity.EmailBody
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserByEmail
import com.book.user.domain.api.operation.GetUserByEmail.Error.UserNotFound
import com.book.user.microservice.route.UserRouting.Api
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getUserByEmail() {
    withUserEmailDocumentation()
    get<Api.Internal.User.Email> {
        val getUserByEmail by application.inject<GetUserByEmail>()
        val email = call.receive<EmailBody>()
        call.respondWith(getUserByEmail(email))
    }
}

internal fun Route.withUserEmailDocumentation() {
    install(NotarizedResource<Api.Internal.User.Email>()) {
        tags = setOf("internal")
        get = GetInfo.builder {
            summary("Get user by email")
            description("Get user profile by email.")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<User>()
                description("User associated with this email")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.NotFound)
                responseType<SimpleServerError>(
                    ObjectEnrichment(id = "GetUserByEmail") {
                        SimpleServerError::errorCode {
                            NumberEnrichment("errorCode") {
                                description = buildString {
                                    append("${UserNotFound.code} - ${UserNotFound.message}\n")
                                }
                            }
                        }
                    }
                )
                description("User not found")
            }
        }
    }
}