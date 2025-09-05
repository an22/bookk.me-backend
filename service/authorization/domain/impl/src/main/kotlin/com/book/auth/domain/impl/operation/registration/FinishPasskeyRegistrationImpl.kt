package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.authentication.entity.Authentication
import com.book.auth.domain.api.authentication.entity.FinishRegistrationRequest
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.api.identification.entity.PasskeyCredential.CredentialDescriptor
import com.book.auth.domain.api.registration.operation.FinishPasskeyRegistration
import com.book.auth.domain.api.registration.operation.FinishPasskeyRegistration.Error.ChallengeWindowExpired
import com.book.auth.domain.api.registration.operation.FinishPasskeyRegistration.Error.VerificationFailed
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.book.auth.domain.repository.CacheableCredentialRepository
import com.bookk.core.toUUID
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RegistrationResult
import com.yubico.webauthn.data.AuthenticatorAttestationResponse
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.exception.RegistrationFailedException
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Clock
import kotlin.uuid.Uuid

private typealias PKS = PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>

internal class FinishPasskeyRegistrationImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val credentialRepository: CacheableCredentialRepository
) : FinishPasskeyRegistration {
    override suspend fun verifyRequest(request: FinishRegistrationRequest): Result<PasskeyCredential> = runCatching {
        val pkc = PublicKeyCredential.parseRegistrationResponseJson(request.publicKeyCredentialJson)
        val challengeJson = passKeyDataSource.getCachedChallenge(request.requestId) ?: throw ChallengeWindowExpired
        val challenge = PublicKeyCredentialCreationOptions.fromJson(challengeJson)
        validateRegistrationChallenge(request, challenge, pkc).asPasskeyCredential(pkc, challenge)
    }.recoverCatching {
        when (it) {
            is RegistrationFailedException -> throw VerificationFailed
            else -> throw it
        }
    }

    override suspend fun attachOwner(ownerId: Uuid, passkey: PasskeyCredential): Result<Unit> = runCatching {
        val passkeyWithOwner = passkey.copy(authId = ownerId)
        passKeyDataSource.createPasskeyCredential(passkeyWithOwner)
    }

    private suspend fun validateRegistrationChallenge(
        request: FinishRegistrationRequest,
        challenge: PublicKeyCredentialCreationOptions,
        response: PKS
    ): RegistrationResult {
        passKeyDataSource.deleteCachedChallenge(request.requestId)
        cacheRepositoryData(response)
        return createRelyingParty(credentialRepository)
            .finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(challenge)
                    .response(response)
                    .build()
            )
    }

    private suspend fun cacheRepositoryData(response: PKS) {
        credentialRepository.lookupAllCache(response.id)
    }

    @Suppress("DEPRECATION")
    private fun RegistrationResult.asPasskeyCredential(pkc: PKS, challenge: PublicKeyCredentialCreationOptions): PasskeyCredential {
        return PasskeyCredential(
            id = Uuid.random(),
            authId = Uuid.random(),
            authInfo = Authentication(Uuid.random(), Uuid.random(), Uuid.random()), //Ignored
            name = challenge.user.displayName,
            credDescriptor = CredentialDescriptor(
                id = keyId.id.bytes,
                type = keyId.type.id,
                transports = keyId.transports.getOrNull()?.map { it.id }.orEmpty().toSet()
            ),
            publicKey = publicKeyCose.base64,
            signatureCount = signatureCount,
            isDiscoverable = isDiscoverable.getOrElse { false },
            isBackupEligible = isBackupEligible,
            isBackedUp = isBackedUp,
            attestationObject = pkc.response.attestationObject.bytes,
            clientData = pkc.response.clientDataJSON.bytes.decodeToString(),
            handle = challenge.user.id.bytes.toUUID(),
            createdAt = Clock.System.now(),
            lastUsedAt = Clock.System.now()
        )
    }
}