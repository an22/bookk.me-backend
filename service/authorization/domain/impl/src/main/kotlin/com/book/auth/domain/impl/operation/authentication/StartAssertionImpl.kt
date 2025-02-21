package com.book.auth.domain.impl.operation.authentication

import com.book.auth.domain.api.authentication.entity.AssertionStartResponse
import com.book.auth.domain.api.authentication.operation.StartAssertion
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.StartAssertionOptions
import com.yubico.webauthn.data.UserVerificationRequirement
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

internal class StartAssertionImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val credentialsRepository: CredentialRepository
) : StartAssertion {
    override suspend fun invoke(): Result<AssertionStartResponse> = runCatching {
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
        AssertionStartResponse(
            requestId = requestUUID,
            challengeJson = request.toCredentialsGetJson()
        )
    }
}