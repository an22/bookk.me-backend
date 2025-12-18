package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.error.AuthErrorCodes
import com.bookk.auth.domain.api.registration.entity.CreateAccountRequest
import com.bookk.auth.domain.api.registration.entity.RegistrationChallengeResponse
import com.bookk.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.bookk.auth.domain.api.registration.operation.FinishRegistration
import com.bookk.auth.domain.api.registration.operation.StartRegistration
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.enity.respondWith
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.registration() {
    /**
     * Get sign up challenge
     * @description Get sign up challenge for passkey creation.
     * @tag *auth
     * @body application/protobuf [CreateAccountRequest] User parameters
     * @response 200 application/protobuf [RegistrationChallengeResponse] Validation challenge returned
     * @response 422 application/protobuf [SimpleServerError]
     * Codes:
     *  1. [AuthErrorCodes.EMAIL_EXIST] - This email already exists
     *  2. [AuthErrorCodes.INVALID_EMAIL_FORMAT] - Invalid email format
     */
    post<Api.Auth.PassKey.SignUpChallenge> {
        val info = call.receive<CreateAccountRequest>()
        val startRegistration by application.inject<StartRegistration>()

        call.respondWith(startRegistration(info))
    }
    /**
     * Validate passkey data
     * @description Validate passkey data from native credentials API
     * @tag *auth
     * @body application/protobuf [VerifyAccountCreationRequest] User parameters
     * @response 200 application/protobuf [AuthTokens] User has been created
     * @response 422 application/protobuf [SimpleServerError]
     * Codes:
     *  1. [AuthErrorCodes.USER_ALREADY_EXIST] - User with this email already exist
     *  2. [AuthErrorCodes.INVALID_EMAIL_FORMAT] - Invalid email format
     *  3. [AuthErrorCodes.ACCOUNT_CREATION_FAILED] - Error during account creation, try again later
     *  4. [AuthErrorCodes.CHALLENGE_WINDOW_EXPIRED] - Challenge window expired
     *  5. [AuthErrorCodes.VERIFICATION_FAILED] - Passkey verification failed
     */
    post<Api.Auth.SignUp> {
        val info = call.receive<VerifyAccountCreationRequest>()
        val finishRegistration by application.inject<FinishRegistration>()
        call.respondWith(finishRegistration(info))
    }
}