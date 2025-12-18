package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.error.AuthErrorCodes
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.domain.api.token.entity.RefreshTokenInfo
import com.bookk.auth.domain.api.token.operation.RefreshToken
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.auth.RefreshPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postRefreshToken() {
    /**
     * Refresh access token
     * @description This request generates new access/refresh pair.
     * @tag *auth
     * @security jwt
     * @body application/protobuf [RefreshTokenInfo] Refresh token info
     * @response 200 application/protobuf [AuthTokens] New access/refresh pair
     * @response 422 application/protobuf [SimpleServerError] Code: [AuthErrorCodes.INVALID_CREDENTIALS] - Invalid refresh token
     */
    authenticate("refresh") {
        post<Api.Auth.Refresh> {
            val principal = requireNotNull(call.principal<RefreshPrincipal>())
            val refreshToken by application.inject<RefreshToken>()
            call.respondWith(refreshToken(RefreshTokenInfo(principal.tokenId, principal.deviceId)))
        }
    }
}