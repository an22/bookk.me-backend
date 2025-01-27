package com.book.auth.domain.impl.operation.authentication

import com.book.auth.domain.api.authentication.entity.SignInStartResponse
import com.book.auth.domain.api.authentication.operation.StartSignIn
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.StartAssertionOptions
import com.yubico.webauthn.data.UserVerificationRequirement
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

internal class StartSignInImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val credentialsRepository: CredentialRepository
) : StartSignIn {
    override suspend fun invoke(): Result<SignInStartResponse> = runCatching {
        val requestUUID = UUID.randomUUID().toString()
        val request = createRelyingParty(credentialsRepository)
            .startAssertion(
                StartAssertionOptions
                    .builder()
                    .userVerification(UserVerificationRequirement.REQUIRED)
                    .timeout(5.minutes.inWholeMilliseconds)
                    .build()
            )
        passKeyDataSource.saveChallengeToCache(requestUUID, request.toJson())
        SignInStartResponse(
            requestId = requestUUID,
            challengeJson = request.toCredentialsGetJson()
        )
    }
}