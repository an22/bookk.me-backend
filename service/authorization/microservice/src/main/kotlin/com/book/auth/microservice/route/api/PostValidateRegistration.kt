package com.book.auth.microservice.route.api

import com.book.auth.domain.api.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.operation.FinishRegistration
import com.book.auth.domain.api.operation.StartRegistration.CreateUserAccountError
import com.book.auth.domain.api.routing.AuthRouting.Api
import com.book.core.domain.entity.handle
import com.book.core.service.applyMediaType
import com.book.core.service.enity.MessageServerError
import com.book.core.service.enity.SimpleServerError
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject
import kotlin.reflect.typeOf

internal fun Route.postValidateRegistration() {
    withValidateRegistrationDocumentation()
    post<Api.Auth.SignUp.PassKey.Validate> {
        val info = call.receive<VerifyAccountCreationRequest>()
        val finishRegistration by application.inject<FinishRegistration>()
        finishRegistration(info)
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
                responseType(typeOf<Unit>())
                description("User has been created")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.BadRequest)
                responseType<SimpleServerError>()
                description("Validation failed")
            }
        }
    }
}