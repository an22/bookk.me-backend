package com.book.auth.microservice.route.api

import com.book.auth.domain.api.identification.entity.AddPasskeyRequest
import com.book.auth.domain.api.identification.entity.PasskeyResponse
import com.book.auth.domain.api.identification.operation.DeletePasskey
import com.book.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge
import com.book.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge.Error.UnableToGeneratePasskeyChallenge
import com.book.auth.domain.api.identification.operation.GetAvailablePasskeys
import com.book.auth.domain.api.registration.entity.RegistrationChallengeResponse
import com.book.auth.domain.api.registration.operation.AttachNewPasskeyToAccount
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
    authenticate {
        withPasskeyListDocumentation()
        get<Api.Auth.PassKey> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getPasskeys by application.inject<GetAvailablePasskeys>()

            call.respondWith(getPasskeys(principal.authId))
        }
        withAddPasskeyChallengeDocumentation()
        get<Api.Auth.PassKey.AddChallenge> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getChallenge by application.inject<GetAttachPasskeyToAccountChallenge>()

            call.respondWith(getChallenge(principal.authId, principal.deviceId, principal.userId))
        }
        withAddPasskeyChallengeVerificationDocumentation()
        post<Api.Auth.PassKey.AddFinish> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AddPasskeyRequest>()
            val attachPasskey by application.inject<AttachNewPasskeyToAccount>()

            call.respondWith(attachPasskey(principal.authId, body))
        }
        withDeletePasskeyDocumentation()
        delete<Api.Auth.PassKey.Id> { path ->
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val deletePasskey by application.inject<DeletePasskey>()
            call.respondWith(deletePasskey(path.id, principal.userId))
        }
    }
}

private fun Route.withPasskeyListDocumentation() {
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
    }
}

private fun Route.withDeletePasskeyDocumentation() {
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

private fun Route.withAddPasskeyChallengeDocumentation() {
    install(NotarizedResource<Api.Auth.PassKey.AddChallenge>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        get = GetInfo.builder {
            summary("Get challenge for new passkey")
            description("Creates challenge for process of attaching passkey to current account")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<RegistrationChallengeResponse>()
                description("New challenge")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.UnprocessableEntity)
                responseType<SimpleServerError>(ObjectEnrichment(id = "AddPasskeyChallenge") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            description = buildString {
                                append("${UnableToGeneratePasskeyChallenge.code} - ${UnableToGeneratePasskeyChallenge.message}\n")
                            }
                        }
                    }
                })
                description("Unprocessable entity")
            }
        }
    }
}

private fun Route.withAddPasskeyChallengeVerificationDocumentation() {
    install(NotarizedResource<Api.Auth.PassKey.AddFinish>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
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
}