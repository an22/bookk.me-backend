package com.book.auth.microservice.route

import com.book.auth.domain.api.entity.RefreshTokenInfo
import com.book.auth.domain.api.entity.SignInInfo
import com.book.auth.domain.api.entity.SignUpInfo
import com.book.auth.domain.api.operation.CreateUserAccount
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.GenerateAuthToken.GenerateAuthTokenBusinessError
import com.book.auth.domain.api.operation.RefreshToken
import com.book.core.domain.entity.handle
import com.book.core.service.enity.MessageServerError
import com.book.core.service.enity.SimpleServerError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Routing.authRoute() {
    route("/auth") {
        route("/sign_in") {
            signInDocumentation()
            post {
                val generator by application.inject<GenerateAuthToken>()
                val info = call.receive<SignInInfo>()
                generator.call(GenerateAuthToken.Param.FromCredentials(info))
                    .handle<_,GenerateAuthTokenBusinessError>(
                        onSuccess = {
                            call.respond(it)
                        },
                        onBusinessError = {
                            call.respond(HttpStatusCode.BadRequest, SimpleServerError("Invalid credentials", it.code))
                        },
                        onUnexpectedError = {
                            call.respond(HttpStatusCode.InternalServerError, MessageServerError(it.message.orEmpty()))
                        }
                    )
            }
        }
        route("/sign_up") {
            signUpDocumentation()
            post {
                val generator by application.inject<CreateUserAccount>()
                val info = call.receive<SignUpInfo>()
                generator.call(info)
                    .handle<_, CreateUserAccount.CreateUserAccountError>(
                        onSuccess = {
                            call.respond(HttpStatusCode.Created, it)
                        },
                        onBusinessError = {
                            call.respond(HttpStatusCode.BadRequest, SimpleServerError(it.errorMessage.orEmpty(), it.code))
                        },
                        onUnexpectedError = {
                            call.respond(HttpStatusCode.InternalServerError, MessageServerError(it.message.orEmpty()))
                        }
                    )
            }
        }
        route("/refresh") {
            refreshDocumentation()
            post {
                val operation by application.inject<RefreshToken>()
                val info = call.receive<RefreshTokenInfo>()
                operation.call(info)
                    .handle<_, RefreshToken.RefreshTokenError>(
                        onSuccess = {
                            call.respond(it)
                        },
                        onBusinessError = {
                            call.respond(HttpStatusCode.Unauthorized, SimpleServerError(it.errorMessage.orEmpty(), it.code))
                        },
                        onUnexpectedError = {
                            call.respond(HttpStatusCode.InternalServerError, MessageServerError(it.message.orEmpty()))
                        }
                    )
            }
        }
        authenticate {
            route("/sign_out") {
                signOutDocumentation()

            }
        }
    }
}