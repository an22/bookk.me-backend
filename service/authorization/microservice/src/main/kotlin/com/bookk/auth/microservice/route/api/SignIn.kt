package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.authentication.entity.AssertionStartResponse
import com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest
import com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest.DeviceInfo
import com.bookk.auth.domain.api.authentication.operation.SignIn
import com.bookk.auth.domain.api.authentication.operation.StartAssertion
import com.bookk.auth.domain.api.error.AuthErrorCodes
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.enity.respondWith
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.signIn() {
    /**
     * Get sign in challenge
     * @description Get sign in passkey challenge.
     * @tag *auth
     * @response 200 application/protobuf [AssertionStartResponse] Passkey json payload
     */
    get<Api.Auth.PassKey.SignInChallenge> {
        val startAssertion: StartAssertion by application.inject()
        call.respondWith(startAssertion())
    }
    /**
     * Verify sign in
     * @description Verify user-signed challenge and return authentication tokens
     * @tag *auth
     * @request application/protobuf [VerifySignInRequest]
     * @response 200 application/protobuf [AuthTokens] Authentication tokens
     * @response 422 application/protobuf [SimpleServerError]
     * Codes:
     *  1. [AuthErrorCodes.CHALLENGE_WINDOW_EXPIRED] - Challenge window expired
     *  2. [AuthErrorCodes.VERIFICATION_FAILED] - Passkey verification failed
     *  3. [AuthErrorCodes.PASSKEY_OWNER_NOT_FOUND] - Passkey owner not found
     */
    post<Api.Auth.SignIn> {
        val request = call.receive<VerifySignInRequest>()
        val startSigningIn: SignIn by application.inject()
        call.respondWith(startSigningIn(request))
    }

    /**
     * Verify sign in
     * @description Verify user-signed challenge and return authentication tokens
     * @tag *auth
     * @request application/protobuf [DeviceInfo]
     * @response 200 application/protobuf [AuthTokens] Authentication tokens
     * @response 422 application/protobuf [SimpleServerError]
     */
    post<Api.Auth.SignIn> {
        val deviceInfo = call.receive<DeviceInfo>()
        val startSigningIn: SignIn by application.inject()
        call.respondWith(startSigningIn(deviceInfo))
    }
}