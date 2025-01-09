package com.book.auth.microservice.route.api

import com.book.auth.domain.api.entity.CreateAccountRequest
import com.book.auth.domain.api.operation.StartRegistration
import com.book.auth.domain.api.operation.StartRegistration.CreateUserAccountError
import com.book.auth.domain.api.operation.StartRegistration.CreateUserAccountError.EmailAlreadyExist
import com.book.auth.domain.api.routing.AuthRouting.Api
import com.book.core.domain.entity.handle
import com.book.core.service.applyMediaType
import com.book.core.service.enity.MessageServerError
import com.book.core.service.enity.SimpleServerError
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject
import kotlin.reflect.typeOf

internal fun Route.postSignUpChallenge() {
    withSignUpChallengeDocumentation()
    post<Api.Auth.SignUp.PassKey.Challenge> {
        val info = call.receive<CreateAccountRequest>()
        val startRegistration by application.inject<StartRegistration>()
        startRegistration(info)
            .handle<_, CreateUserAccountError>(
                onSuccess = {
                    call.respond(HttpStatusCode.OK, it)
                },
                onBusinessError = {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SimpleServerError(it.errorMessage.orEmpty(), it.code)
                    )
                },
                onUnexpectedError = {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MessageServerError(it.message.orEmpty())
                    )
                }
            )
    }
}

internal fun Route.withSignUpChallengeDocumentation() {
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
                responseType(typeOf<Unit>())
                description("User has been created")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.BadRequest)
                responseType<SimpleServerError>(ObjectEnrichment(id = "GetSignUpChallenge") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            minimum = 1
                            maximum = 1
                            description = buildString {
                                append("${EmailAlreadyExist.code} - ${EmailAlreadyExist.errorMessage}")
                            }
                        }
                    }
                })
                description("User already registered")
            }
        }
    }
}