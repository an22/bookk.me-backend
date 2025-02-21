package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.registration.entity.CreateAccountRequest
import com.book.auth.domain.api.registration.entity.SignUpChallengeResponse
import com.book.auth.domain.api.registration.operation.StartRegistration
import com.book.auth.domain.api.registration.operation.StartRegistration.Error.EmailAlreadyExist
import com.book.auth.domain.api.registration.operation.StartRegistration.Error.InvalidEmailFormat
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.book.core.domain.entity.throwIf
import com.book.user.domain.api.entity.EmailBody
import com.book.user.domain.api.operation.GetUserByEmail
import com.bookk.server.user.client.UserClient
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class StartRegistrationImpl(
    private val userClient: UserClient,
    private val userPassKeyDataSource: PassKeyDataSource,
    private val credentialsRepository: CredentialRepository
) : StartRegistration {

    private val emailRegex = Regex(RegistrationConstants.EMAIL_REGEX)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(request: CreateAccountRequest) = runCatching {
        if (!emailRegex.matches(request.email)) throw InvalidEmailFormat
        userClient.getUserByEmail(EmailBody(request.email))
            .onSuccess { throw EmailAlreadyExist }
            .throwIf { it != GetUserByEmail.Error.UserNotFound }
        val byteArray = ByteArray(64)
        val handle = ByteArray(Random.nextBytes(byteArray))
        val randomNewAuthId = Uuid.random().toString()
        val challenge = createCreationOptions(randomNewAuthId, request, handle)
        userPassKeyDataSource.saveChallengeToCache(randomNewAuthId, challenge.toJson())
        SignUpChallengeResponse(
            challenge = challenge.toCredentialsCreateJson(),
            displayName = "${request.firstName} ${request.lastName}",
            userId = randomNewAuthId
        )
    }

    private fun createCreationOptions(
        authId: String,
        request: CreateAccountRequest,
        handle: ByteArray
    ): PublicKeyCredentialCreationOptions {
        return createRelyingParty(credentialsRepository).startRegistration(
            StartRegistrationOptions.builder()
                .user(
                    UserIdentity.builder()
                        .name(authId)
                        .displayName("${request.firstName} ${request.lastName}")
                        .id(handle)
                        .build()
                )
                .authenticatorSelection(
                    AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .build()
                )
                .build()
        )
    }
}