package com.book.auth.domain.impl.operation.authentication

import com.book.auth.domain.api.authentication.entity.FinishAssertionRequest
import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.api.authentication.operation.FinishAssertion.Error
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.book.auth.domain.repository.CacheableCredentialRepository
import com.bookk.core.toUUID
import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.AssertionResult
import com.yubico.webauthn.FinishAssertionOptions
import com.yubico.webauthn.data.AuthenticatorAssertionResponse
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.exception.AssertionFailedException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class FinishAssertionImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val credentialRepository: CacheableCredentialRepository,
) : FinishAssertion {
    override suspend fun invoke(request: FinishAssertionRequest): Result<PasskeyCredential> = runCatching {
        val cachedRequest = passKeyDataSource.getCachedChallenge(request.requestId) ?: throw Error.ChallengeWindowExpired
        val challenge = AssertionRequest.fromJson(cachedRequest)
        val response = PublicKeyCredential.parseAssertionResponseJson(request.publicKeyCredentialJson)
        passKeyDataSource.deleteCachedChallenge(request.requestId)
        cacheRepositoryData(challenge, response)
        val result: AssertionResult = createRelyingParty(credentialRepository)
            .finishAssertion(
                FinishAssertionOptions.builder()
                    .request(challenge)
                    .response(response)
                    .build()
            )
        if (result.isSuccess) {
            val credentials = passKeyDataSource.getCredentialBy(
                userHandle = result.credential.userHandle.bytes.toUUID(),
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

    private suspend fun cacheRepositoryData(
        challenge: AssertionRequest,
        response: PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
    ) {
        coroutineScope {
            listOf(
                async { credentialRepository.cacheUsername(challenge.userHandle.get()) },
                async { credentialRepository.cacheUserHandle(challenge.username.get()) },
                async { credentialRepository.lookupCache(response.id, challenge.userHandle.get()) }
            )
        }.awaitAll()
    }
}