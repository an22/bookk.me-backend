package com.book.auth.microservice.route.api

import com.book.auth.domain.api.entity.RefreshTokenInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.operation.RefreshToken
import com.book.auth.domain.api.operation.RefreshToken.Error.InvalidRefreshToken
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

internal fun Route.postRefreshToken() {
    withRefreshDocumentation()
    post<Api.Auth.Refresh> {
        val info = call.receive<RefreshTokenInfo>()
        val refreshToken by application.inject<RefreshToken>()
        call.respondWith(refreshToken(info))
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
                responseType<TokenInfo>()
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