package com.book.auth.microservice.route.api

import com.book.auth.domain.api.entity.SignUpInfo
import com.book.auth.domain.api.operation.CreateUserAccount
import com.book.auth.domain.api.operation.CreateUserAccount.CreateUserAccountError.EmailOrPhoneAlreadyExist
import com.book.auth.domain.api.operation.CreateUserAccount.CreateUserAccountError.LoginAlreadyExist
import com.book.auth.domain.api.operation.CreateUserAccount.CreateUserAccountError.PasswordTooShort
import com.book.auth.domain.api.operation.CreateUserAccount.CreateUserAccountError.PasswordTooWeak
import com.book.auth.domain.api.routing.AuthRouting.Api
import com.book.auth.domain.impl.operation.MIN_PASSWORD_LENGTH
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

internal fun Route.postCreateAccount() {
    withSignUpDocumentation()
    post<Api.Auth.SignUp> {
        val generator by application.inject<CreateUserAccount>()
        val info = call.receive<SignUpInfo>()
        generator.call(info)
            .handle<_, CreateUserAccount.CreateUserAccountError>(
                onSuccess = {
                    call.respond(HttpStatusCode.Created, it)
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

internal fun Route.withSignUpDocumentation() {
    install(NotarizedResource<Api.Auth.SignUp>()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Sign Up")
            description("Create new user account.")
            request {
                applyMediaType()
                requestType<SignUpInfo>()
                description("User parameters")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.Created)
                responseType(typeOf<Unit>())
                description("User has been created")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.BadRequest)
                responseType<SimpleServerError>(ObjectEnrichment(id = "CreateUser") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            minimum = 1
                            maximum = 4
                            description = buildString {
                                append("${EmailOrPhoneAlreadyExist.code} - ${EmailOrPhoneAlreadyExist.errorMessage}")
                                append("\n\n")
                                append("${LoginAlreadyExist.code} - ${LoginAlreadyExist.errorMessage}")
                                append("\n\n")
                                append(
                                    "${PasswordTooShort.code} - ${
                                        PasswordTooShort.errorMessage.orEmpty().format(MIN_PASSWORD_LENGTH)
                                    }"
                                )
                                append("\n\n")
                                append("${PasswordTooWeak.code} - ${PasswordTooWeak.errorMessage}")
                            }
                        }
                    }
                })
                description("User not found for provided credentials")
            }
        }
    }
}