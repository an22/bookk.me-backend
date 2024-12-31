package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.PassKeyDataSource
import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.entity.ChallengeResponse
import com.book.auth.domain.api.entity.PassKeySignUpStartInfo
import com.book.auth.domain.api.operation.StartRegistration
import com.book.auth.domain.api.operation.StartRegistration.CreateUserAccountError
import com.bookk.core.AppLevelConstants
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.RelyingPartyIdentity
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import kotlin.random.Random

internal class StartRegistrationImpl(
    private val authDataSource: UserAuthDataSource,
    private val userPassKeyDataSource: PassKeyDataSource,
    credentialsRepository: CredentialRepository
) : StartRegistration {

    private val rp: RelyingParty = RelyingParty.builder()
        .identity(
            RelyingPartyIdentity.builder()
                .id(AppLevelConstants.DOMAIN_NAME)
                .name(AppLevelConstants.APP_NAME)
                .build()
        )
        .credentialRepository(credentialsRepository)
        .build()

    override suspend fun call(params: PassKeySignUpStartInfo): Result<ChallengeResponse> = runCatching {
        val userRecord = authDataSource.getAuthRecordByUsername(params.email)
        if (userRecord != null) throw CreateUserAccountError.EmailAlreadyExist
        val byteArray = ByteArray(64)
        val handle = ByteArray(Random.nextBytes(byteArray))
        val credentialCreationOptions = rp.startRegistration(
            StartRegistrationOptions.builder()
                .user(
                    UserIdentity.builder()
                        .name(params.email)
                        .displayName("${params.firstName} ${params.lastName}")
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
        val challenge = credentialCreationOptions.toCredentialsCreateJson()
        userPassKeyDataSource.saveCredentialOptions(handle.base64, challenge)
        ChallengeResponse(challenge)
    }
}