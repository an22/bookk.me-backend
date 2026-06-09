package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.registration.entity.CreateAccountRequest
import com.bookk.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.bookk.auth.domain.api.registration.operation.FinishRegistration
import com.bookk.auth.domain.api.registration.operation.StartRegistration
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.service.enity.respondWith
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.registration() {
    /**
     * Summary: Get sign up challenge
     * Description: Get sign up challenge for passkey creation.
     * Tag: auth
     * Body: application/x-protobuf [com.bookk.auth.domain.api.registration.entity.CreateAccountRequest] User parameters
     * Response: 200 application/x-protobuf [com.bookk.auth.domain.api.registration.entity.RegistrationChallengeResponse] Validation challenge returned
     * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Registration errors:
     *  - EMAIL_EXIST (Code 200001): This email already exists
     *  - INVALID_EMAIL_FORMAT (Code 200002): Invalid email format
     */
    post<Api.Auth.PassKey.SignUpChallenge> {
        val info = call.receive<CreateAccountRequest>()
        val startRegistration by application.inject<StartRegistration>()

        call.respondWith(startRegistration(info))
    }
    /**
     * Summary: Validate passkey data
     * Description: Validate passkey data from native credentials API
     * Tag: auth
     * Body: application/x-protobuf [com.bookk.auth.domain.api.registration.entity.VerifyAccountCreationRequest] User parameters
     * Response: 200 application/x-protobuf [com.bookk.auth.domain.api.token.entity.AuthTokens] User has been created
     * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Registration errors:
     *  - USER_ALREADY_EXIST (Code 200003): User with this email already exist
     *  - INVALID_EMAIL_FORMAT (Code 200002): Invalid email format
     *  - ACCOUNT_CREATION_FAILED (Code 200004): Error during account creation, try again later
     *  - CHALLENGE_WINDOW_EXPIRED (Code 300001): Challenge window expired
     *  - VERIFICATION_FAILED (Code 300002): Passkey verification failed
     */
    post<Api.Auth.SignUp> {
        val info = call.receive<VerifyAccountCreationRequest>()
        val finishRegistration by application.inject<FinishRegistration>()
        call.respondWith(finishRegistration(info))
    }
}
