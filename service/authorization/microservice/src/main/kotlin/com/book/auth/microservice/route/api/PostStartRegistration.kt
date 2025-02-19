package com.book.auth.microservice.route.api

import com.book.auth.domain.api.registration.entity.CreateAccountRequest
import com.book.auth.domain.api.registration.entity.SignUpChallengeResponse
import com.book.auth.domain.api.registration.operation.StartRegistration
import com.book.auth.domain.api.registration.operation.StartRegistration.Error.EmailAlreadyExist
import com.book.auth.domain.api.registration.operation.StartRegistration.Error.InvalidEmailFormat
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.core.domain.entity.SimpleServerError
import com.book.core.service.applyMediaType
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject
import kotlin.reflect.typeOf

internal fun Route.postStartRegistration() {
    withStartRegistrationDocumentation()
    post<Api.Auth.SignUp.PassKey.Challenge> {
        val info = call.receive<CreateAccountRequest>()
        val startRegistration by application.inject<StartRegistration>()

        call.respondWith(startRegistration(info))
    }
}

internal fun Route.withStartRegistrationDocumentation() {
    install(NotarizedResource<Api.Auth.SignUp.PassKey.Challenge>()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Get sign up challenge")
            description("Get sign up challenge for passkey creation.")
            request {
                applyMediaType()
                requestType<CreateAccountRequest>()
                description("User parameters")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<SignUpChallengeResponse>())
                description("Validation challenge returned")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.UnprocessableEntity)
                responseType<SimpleServerError>(ObjectEnrichment(id = "GetSignUpChallenge") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            description = buildString {
                                append("${EmailAlreadyExist.code} - ${EmailAlreadyExist.message}\n")
                                append("${InvalidEmailFormat.code} - ${InvalidEmailFormat.message}\n")
                            }
                        }
                    }
                })
                description("Unprocessable entity")
            }
        }
    }
}