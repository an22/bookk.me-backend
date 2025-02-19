package com.book.auth.microservice.route.api

import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.entity.RefreshTokenInfo
import com.book.auth.domain.api.token.operation.RefreshToken
import com.book.auth.domain.api.token.operation.RefreshToken.Error.InvalidRefreshToken
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.core.domain.entity.SimpleServerError
import com.book.core.service.applyMediaType
import com.book.core.service.auth.RefreshPrincipal
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postRefreshToken() {
    withRefreshDocumentation()
    authenticate("refresh") {
        post<Api.Auth.Refresh> {
            val principal = requireNotNull(call.principal<RefreshPrincipal>())
            val refreshToken by application.inject<RefreshToken>()
            call.respondWith(refreshToken(RefreshTokenInfo(principal.tokenId, principal.deviceId)))
        }
    }
}

internal fun Route.withRefreshDocumentation() {
    install(NotarizedResource<Api.Auth.Refresh>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        post = PostInfo.builder {
            summary("Refresh access token")
            description("This request will generate new access/refresh pair.")
            request {
                applyMediaType()
                requestType<RefreshTokenInfo>()
                description("Refresh token info")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<AuthTokens>()
                description("New access/refresh pair")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.BadRequest)
                description("Bad request")
                responseType(
                    ObjectEnrichment<SimpleServerError>(id = "RefreshToken") {
                        SimpleServerError::errorCode {
                            NumberEnrichment("errorCode") {
                                minimum = 1
                                maximum = 1
                                description = buildString {
                                    append("${InvalidRefreshToken.code} - ${InvalidRefreshToken.message}")
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}