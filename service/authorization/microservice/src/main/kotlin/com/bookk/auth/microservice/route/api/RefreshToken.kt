package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.token.entity.RefreshTokenInfo
import com.bookk.auth.domain.api.token.operation.RefreshToken
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.service.auth.RefreshPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postRefreshToken() {
    authenticate("refresh") {
        /**
         * Summary: Refresh access token
         * Description: This request generates new access/refresh pair.
         * Tag: auth
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.auth.domain.api.token.entity.RefreshTokenInfo] Refresh token info
         * Response: 200 application/x-protobuf [com.bookk.auth.domain.api.token.entity.AuthTokens] New access/refresh pair
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Refresh errors<br>INVALID_CREDENTIALS (Code 400001) Invalid refresh token
         */
        post<Api.Auth.Refresh> {
            val principal = requireNotNull(call.principal<RefreshPrincipal>())
            val refreshToken by application.inject<RefreshToken>()
            call.respondWith(refreshToken(RefreshTokenInfo(principal.tokenId, principal.deviceId)))
        }
    }
}
