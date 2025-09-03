package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.registration.entity.RegistrationChallengeResponse
import com.book.auth.domain.api.registration.operation.StartPasskeyRegistration
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import kotlin.uuid.Uuid
import com.yubico.webauthn.data.ByteArray as YubicoByteArray

internal class StartPasskeyRegistrationImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val credentialRepository: CredentialRepository
) : StartPasskeyRegistration {

    override suspend fun invoke(userHandle: Uuid, passkeyDisplayName: String): Result<RegistrationChallengeResponse> = runCatching {
        val requestId = Uuid.random().toString()
        val challenge = createChallenge(requestId, passkeyDisplayName, YubicoByteArray(userHandle.toByteArray()))
        passKeyDataSource.saveChallengeToCache(requestId, challenge.toJson())
        RegistrationChallengeResponse(
            requestId = requestId,
            challenge = challenge.toCredentialsCreateJson(),
            displayName = passkeyDisplayName
        )
    }

    private fun createChallenge(
        userName: String,
        displayName: String,
        handle: YubicoByteArray
    ): PublicKeyCredentialCreationOptions {
        return createRelyingParty(credentialRepository).startRegistration(
            StartRegistrationOptions.builder()
                .user(
                    UserIdentity.builder()
                        .name(userName)
                        .displayName(displayName)
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