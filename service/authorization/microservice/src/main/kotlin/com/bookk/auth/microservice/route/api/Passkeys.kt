package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.error.AuthErrorCodes
import com.bookk.auth.domain.api.identification.entity.AddPasskeyRequest
import com.bookk.auth.domain.api.identification.entity.PasskeyResponse
import com.bookk.auth.domain.api.identification.operation.DeletePasskey
import com.bookk.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge
import com.bookk.auth.domain.api.identification.operation.GetAvailablePasskeys
import com.bookk.auth.domain.api.registration.entity.RegistrationChallengeResponse
import com.bookk.auth.domain.api.registration.operation.AttachNewPasskeyToAccount
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
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
        /**
         * Get Passkeys
         * @description Get list of all available passkeys for current auth method authId
         * @tag *auth
         * @security jwt
         * @response 200 [PasskeyResponse]
         */
        get<Api.Auth.PassKey> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getPasskeys by application.inject<GetAvailablePasskeys>()

            call.respondWith(getPasskeys(principal.authId))
        }
        /**
         * Get challenge for new passkey
         * @description Creates challenge for process of attaching passkey to current account
         * @tag *auth
         * @security jwt
         * @response 200 application/protobuf [RegistrationChallengeResponse] Registration challenge
         * @response 422 application/protobuf [SimpleServerError] Code: [AuthErrorCodes.UNABLE_TO_GENERATE_REGISTRATION_CHALLENGE] - Unable to provide registration challenge
         */
        get<Api.Auth.PassKey.AddChallenge> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getChallenge by application.inject<GetAttachPasskeyToAccountChallenge>()

            call.respondWith(getChallenge(principal.authId, principal.deviceId, principal.userId))
        }
        /**
         * Add new passkey
         * @description Adds new passkey to account represented by current auth method
         * @tag *auth
         * @security jwt
         * @body application/protobuf [AddPasskeyRequest] Passkey verification payload
         * @response 201 Passkey created
         * @response 422 application/protobuf [SimpleServerError] Code: [AuthErrorCodes.LAST_PASSKEY] - Can't delete passkey if it's the only one registered.
         */
        post<Api.Auth.PassKey.AddFinish> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AddPasskeyRequest>()
            val attachPasskey by application.inject<AttachNewPasskeyToAccount>()

            call.respondWith(attachPasskey(principal.authId, body))
        }
        /**
         * Delete passkey
         * @description Delete passkey by id
         * @tag *auth
         * @security jwt
         * @response 422 application/protobuf [SimpleServerError] Code: [com.bookk.auth.domain.api.error.AuthErrorCodes.LAST_PASSKEY] - Can't delete passkey if it's the only one registered.
         * @response 404 Not found
         * @response 204 Passkey deleted
         */
        delete<Api.Auth.PassKey.Id> { path ->
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val deletePasskey by application.inject<DeletePasskey>()
            call.respondWith(deletePasskey(path.id, principal.userId))
        }
    }
}