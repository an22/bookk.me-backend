package com.book.auth.microservice.route.api

import com.book.auth.domain.api.identification.entity.AddPasskeyRequest
import com.book.auth.domain.api.identification.entity.DeletePasskeyRequest
import com.book.auth.domain.api.identification.entity.PasskeyResponse
import com.book.auth.domain.api.identification.operation.AddPasskey
import com.book.auth.domain.api.identification.operation.DeletePasskey
import com.book.auth.domain.api.identification.operation.GetAvailablePasskeys
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.auth.microservice.route.api.documentation.common.challengeVerificationEnrichment
import com.book.core.domain.entity.SimpleServerError
import com.book.core.service.applyMediaType
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.passkeyOperations() {
    withPasskeysDocumentation()
    authenticate {
        get<Api.Auth.PassKey> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getPasskeys by application.inject<GetAvailablePasskeys>()

            call.respondWith(getPasskeys(principal.authId))
        }
        post<Api.Auth.PassKey> {
            val body = call.receive<AddPasskeyRequest>()
            val addPasskey by application.inject<AddPasskey>()

            call.respondWith(addPasskey(body))
        }
        delete<Api.Auth.PassKey.Id> { path ->
            val deletePasskey by application.inject<DeletePasskey>()
            call.respondWith(deletePasskey(DeletePasskeyRequest(path.id)))
        }
    }
}

private fun Route.withPasskeysDocumentation() {
    install(NotarizedResource<Api.Auth.PassKey>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        get = GetInfo.builder {
            summary("Get Passkeys")
            description("Get list of all available passkeys for current auth method authId")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<PasskeyResponse>()
                description("List of passkeys")
            }
        }
        post = PostInfo.builder {
            summary("Add new passkey")
            description("Adds new passkey to account represented by current auth method")
            request {
                applyMediaType()
                requestType<AddPasskeyRequest>()
                description("Passkey verification payload")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.Created)
                responseType<Unit>()
                description("Passkey created")
            }
            canRespond {
                challengeVerificationEnrichment()
            }
        }
    }
    install(NotarizedResource<Api.Auth.PassKey.Id>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        delete = DeleteInfo.builder {
            summary("Delete passkey")
            description("Delete passkey by id")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.NoContent)
                responseType<Unit>()
                description("Passkey deleted")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.UnprocessableEntity)
                responseType<SimpleServerError>(ObjectEnrichment(id = "DeletePasskey") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            description = buildString {
                                append("${DeletePasskey.Error.LastPasskey.code} - ${DeletePasskey.Error.LastPasskey.message}\n")
                            }
                        }
                    }
                })
                description("Passkey deleted")
            }
        }
    }
}