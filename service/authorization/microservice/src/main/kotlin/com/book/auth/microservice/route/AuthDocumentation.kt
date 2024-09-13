package com.book.auth.microservice.route

import com.book.auth.domain.api.entity.*
import com.book.auth.domain.api.operation.CreateUserAccount.CreateUserAccountError.*
import com.book.auth.domain.api.operation.RefreshToken.RefreshTokenError.InvalidRefreshToken
import com.book.auth.domain.impl.operation.MIN_PASSWORD_LENGTH
import com.book.core.service.enity.SimpleServerError
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.enrichment.TypeEnrichment
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.reflect.typeOf


internal fun Route.signInDocumentation() {
    install(NotarizedRoute()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Sign In")
            description("Get access token and refresh token.")
            request {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                requestType<SignInInfo>()
                description("User auth credentials and current device name")
            }
            response {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.OK)
                responseType<TokenInfo>()
                description("Access token and refresh token")
            }
            canRespond {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.BadRequest)
                responseType<SimpleServerError>()
                description("User not found for provided credentials")
            }
        }
    }
}

internal fun Route.signUpDocumentation() {
    install(NotarizedRoute()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Sign Up")
            description("Create new user account.")
            request {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                requestType<SignUpInfo>()
                description("User parameters")
            }
            response {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.Created)
                responseType(typeOf<Unit>())
                description("User has been created")
            }
            canRespond {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.BadRequest)
                responseType<SimpleServerError>(TypeEnrichment<SimpleServerError>(id = "CreateUser") {
                    SimpleServerError::errorCode {
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
                })
                description("User not found for provided credentials")
            }
        }
    }
}

internal fun Route.refreshDocumentation() {
    install(NotarizedRoute()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        post = PostInfo.builder {
            summary("Refresh access token")
            description("This request will generate new access/refresh pair.")
            request {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                requestType<RefreshTokenInfo>()
                description("Refresh token info")
            }
            response {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.OK)
                responseType<TokenInfo>()
                description("New access/refresh pair")
            }
            canRespond {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.BadRequest)
                description("Bad request")
                responseType(
                    TypeEnrichment<SimpleServerError>(id = "RefreshToken") {
                        SimpleServerError::errorCode {
                            minimum = 1
                            maximum = 1
                            description = buildString {
                                append("${InvalidRefreshToken.code} - ${InvalidRefreshToken.errorMessage}")
                            }
                        }
                    }
                )
            }
        }
    }
}

internal fun Route.signOutDocumentation() {
    install(NotarizedRoute()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        post = PostInfo.builder {
            summary("Sign Out")
            description("Mark user as signed out")
            response {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("Success operation")
            }
        }
    }
}

internal fun Route.deleteAccountDocumentation() {
    install(NotarizedRoute()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        post = PostInfo.builder {
            summary("Delete Account")
            description("Delete user account and all associated data")
            request {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                requestType<DeleteAccountInfo>()
                description("Password with totp code")
            }
            response {
                mediaTypes(ContentType.Application.ProtoBuf.toString())
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("Success operation")
            }
        }
    }
}