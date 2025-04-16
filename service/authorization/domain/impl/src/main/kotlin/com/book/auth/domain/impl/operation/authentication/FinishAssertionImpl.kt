package com.book.auth.domain.impl.operation.authentication

import com.book.auth.domain.api.authentication.entity.FinishAssertionRequest
import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.api.authentication.operation.FinishAssertion.Error
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.AssertionResult
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.FinishAssertionOptions
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.exception.AssertionFailedException

internal class FinishAssertionImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val credentialRepository: CredentialRepository,
) : FinishAssertion {
    override suspend fun invoke(request: FinishAssertionRequest): Result<PasskeyCredential> = runCatching {
        val cachedRequest = passKeyDataSource.getCachedChallenge(request.requestId) ?: throw Error.ChallengeWindowExpired
        val challenge = AssertionRequest.fromJson(cachedRequest)
        val response = PublicKeyCredential.parseAssertionResponseJson(request.publicKeyCredentialJson)
        passKeyDataSource.deleteCachedChallenge(request.requestId)
        val result: AssertionResult = createRelyingParty(credentialRepository)
            .finishAssertion(
                FinishAssertionOptions.builder()
                    .request(challenge)
                    .response(response)
                    .build()
            )
        if (result.isSuccess) {
            val credentials = passKeyDataSource.getCredentialBy(
                userHandle = result.credential.userHandle.bytes,
                credentialId = result.credential.credentialId.bytes
            )
            if (credentials == null) throw Error.PasskeyOwnerNotFound
            passKeyDataSource.markAsUsed(credentials.id)
            return@runCatching credentials
        } else {
            throw Error.PasskeyOwnerNotFound
        }
    }.recoverCatching {
        throw when (it) {
            is AssertionFailedException -> Error.PasskeyOwnerNotFound
            is Error -> it
            else -> Error.VerificationFailed
        }
    }
}