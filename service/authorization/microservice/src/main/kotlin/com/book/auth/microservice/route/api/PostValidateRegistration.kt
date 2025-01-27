package com.book.auth.microservice.route.api

import com.book.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.registration.operation.FinishRegistration
import com.book.auth.domain.api.registration.operation.FinishRegistration.Error.AccountCreationFailed
import com.book.auth.domain.api.registration.operation.FinishRegistration.Error.InvalidEmailFormat
import com.book.auth.domain.api.registration.operation.FinishRegistration.Error.UserAlreadyExist
import com.book.auth.domain.api.registration.operation.FinishRegistration.Error.VerificationFailed
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.core.service.applyMediaType
import com.book.core.service.enity.SimpleServerError
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

internal fun Route.postValidateRegistration() {
    withValidateRegistrationDocumentation()
    post<Api.Auth.SignUp.PassKey.Validate> {
        val info = call.receive<VerifyAccountCreationRequest>()
        val finishRegistration by application.inject<FinishRegistration>()
        call.respondWith(finishRegistration(info))
    }
}

internal fun Route.withValidateRegistrationDocumentation() {
    install(NotarizedResource<Api.Auth.SignUp.PassKey.Validate>()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Validate passkey data")
            description("Validate passkey data from native credentials API")
            request {
                applyMediaType()
                requestType<VerifyAccountCreationRequest>()
                description("User parameters")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType(typeOf<AuthTokens>())
                description("User has been created")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.UnprocessableEntity)
                responseType<SimpleServerError>(ObjectEnrichment(id = "FinishRegistration") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            minimum = 1
                            maximum = 4
                            description = buildString {
                                append("${InvalidEmailFormat.code} - ${InvalidEmailFormat.message}\n")
                                append("${UserAlreadyExist.code} - ${UserAlreadyExist.message}\n")
                                append("${VerificationFailed.code} - ${VerificationFailed.message}\n")
                                append("${AccountCreationFailed.code} - ${AccountCreationFailed.message}\n")
                            }
                        }
                    }
                })
                description("Unprocessable entity")
            }
        }
    }
}