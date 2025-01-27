package com.book.auth.domain.impl.operation.authentication

import com.book.auth.domain.api.authentication.entity.VerifySignInRequest
import com.book.auth.domain.api.authentication.entity.VerifySignInRequest.DeviceInfo
import com.book.auth.domain.api.authentication.operation.FinishSignIn
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.AssertionResult
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.FinishAssertionOptions
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.exception.AssertionFailedException

internal class FinishSignInImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val credentialRepository: CredentialRepository,
    private val generateAuthToken: GenerateAuthToken,
    private val deviceDataSource: DeviceDataSource
) : FinishSignIn {
    override suspend fun invoke(request: VerifySignInRequest): Result<AuthTokens> = runCatching {
        val cachedRequest = passKeyDataSource.getCachedChallenge(request.requestId)
        if (cachedRequest == null) throw FinishSignIn.Error.ChallengeWindowExpired
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
            if (credentials == null) {
                throw FinishSignIn.Error.PasskeyOwnerNotFound
            }
            createDeviceIfNotExist(credentials.authInfo.id, request.deviceInfo)
            generateAuthToken(
                Source.FromAuthDevice(
                    credentials.authInfo.id,
                    request.deviceInfo.deviceUUID
                )
            ).getOrThrow()
        } else {
            throw FinishSignIn.Error.PasskeyOwnerNotFound
        }
    }.recoverCatching {
        throw when (it) {
            is AssertionFailedException -> FinishSignIn.Error.PasskeyOwnerNotFound
            is FinishSignIn.Error -> it
            else -> FinishSignIn.Error.VerificationFailed
        }
    }

    private suspend fun createDeviceIfNotExist(ownerId: Long, deviceInfo: DeviceInfo) {
        deviceDataSource.createDeviceIfNotExist(
            authId = ownerId,
            uuid = deviceInfo.deviceUUID,
            name = deviceInfo.deviceName
        )
    }
}