package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.entity.ChallengeResponse
import com.book.auth.domain.api.entity.CreateAccountRequest
import com.book.auth.domain.api.operation.StartRegistration
import com.book.auth.domain.api.operation.StartRegistration.Error.EmailAlreadyExist
import com.book.auth.domain.api.operation.StartRegistration.Error.InvalidEmailFormat
import com.book.auth.domain.datasource.AccountDataSource
import com.book.auth.domain.datasource.PassKeyDataSource
import com.bookk.core.AppLevelConstants
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import kotlin.random.Random

internal class StartRegistrationImpl(
    private val accountDataSource: AccountDataSource,
    private val userPassKeyDataSource: PassKeyDataSource,
    private val credentialsRepository: CredentialRepository
) : StartRegistration {

    private val emailRegex = Regex(AppLevelConstants.EMAIL_REGEX)

    override suspend fun invoke(request: CreateAccountRequest) = runCatching {
        if (!emailRegex.matches(request.email)) throw InvalidEmailFormat
        val userRecord = accountDataSource.getAuthRecordByEmail(request.email)
        if (userRecord != null) throw EmailAlreadyExist
        val byteArray = ByteArray(64)
        val handle = ByteArray(Random.nextBytes(byteArray))
        val challenge = createCreationOptions(request, handle).toJson()
        userPassKeyDataSource.saveChallengeToCache(handle.base64, challenge)
        ChallengeResponse(
            challenge = challenge,
            displayName = "${request.firstName} ${request.lastName}",
            userId = handle.base64
        )
    }

    private fun createCreationOptions(
        request: CreateAccountRequest,
        handle: ByteArray
    ): PublicKeyCredentialCreationOptions {
        return createRelyingParty(credentialsRepository).startRegistration(
            StartRegistrationOptions.builder()
                .user(
                    UserIdentity.builder()
                        .name(request.email)
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