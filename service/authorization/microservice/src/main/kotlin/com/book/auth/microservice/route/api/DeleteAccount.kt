package com.book.auth.microservice.route.api

import com.book.auth.domain.api.entity.DeleteAccountInfo
import com.book.auth.domain.api.operation.DeleteAccount
import com.book.auth.domain.api.routing.AuthRouting.Api
import com.book.core.domain.entity.handle
import com.book.core.service.applyMediaType
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.MessageServerError
import com.book.core.service.enity.SimpleServerError
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.deleteAccount() {
    withDeleteAccountDocumentation()
    authenticate {
        post<Api.Auth.DeleteAccount> {
            val operation by application.inject<DeleteAccount>()
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val info = call.receive<DeleteAccountInfo>()

            operation.call(DeleteAccount.Param(principal.userName, info))
                .handle<_, DeleteAccount.DeleteAccountError>(
                    onSuccess = {
                        call.respond(HttpStatusCode.OK)
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
}

internal fun Route.withDeleteAccountDocumentation() {
    install(NotarizedResource<Api.Auth.DeleteAccount>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        post = PostInfo.builder {
            summary("Delete Account")
            description("Delete user account and all associated data")
            request {
                applyMediaType()
                requestType<DeleteAccountInfo>()
                description("Password with totp code")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("Success operation")
            }
        }
    }
}