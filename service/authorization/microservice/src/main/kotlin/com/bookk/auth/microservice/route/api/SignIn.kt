package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest
import com.bookk.auth.domain.api.authentication.operation.SignIn
import com.bookk.auth.domain.api.authentication.operation.StartAssertion
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.service.enity.respondWith
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.signIn() {
    /**
     * Summary: Get sign in challenge
     * Description: Get sign in passkey challenge.
     * Tag: auth
     * Response: 200 application/x-protobuf [com.bookk.auth.domain.api.authentication.entity.AssertionStartResponse] Passkey json payload
     */
    get<Api.Auth.PassKey.SignInChallenge> {
        val startAssertion: StartAssertion by application.inject()
        call.respondWith(startAssertion())
    }
    /**
     * Summary: Verify sign in
     * Description: Verify user-signed challenge and return authentication tokens
     * Tag: auth
     * RequestBody: application/x-protobuf [com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest]
     * Response: 200 application/x-protobuf [com.bookk.auth.domain.api.token.entity.AuthTokens] Authentication tokens
     * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Sign in errors:
     *  - CHALLENGE_WINDOW_EXPIRED (Code 300001): Challenge window expired
     *  - VERIFICATION_FAILED (Code 300002): Passkey verification failed
     *  - PASSKEY_OWNER_NOT_FOUND (Code 300003): Passkey owner not found
     */
    post<Api.Auth.SignIn> {
        val request = call.receive<VerifySignInRequest>()
        val startSigningIn: SignIn by application.inject()
        call.respondWith(startSigningIn(request))
    }
}
