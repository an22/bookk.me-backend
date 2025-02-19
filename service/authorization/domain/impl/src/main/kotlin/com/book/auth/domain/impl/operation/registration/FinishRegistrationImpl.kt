package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.authentication.entity.Authentication
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.api.identification.entity.PasskeyCredential.CredentialDescriptor
import com.book.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.registration.entity.VerifyAccountCreationRequest.UserInfo
import com.book.auth.domain.api.registration.operation.FinishRegistration
import com.book.auth.domain.api.registration.operation.FinishRegistration.Error.AccountCreationFailed
import com.book.auth.domain.api.registration.operation.FinishRegistration.Error.VerificationFailed
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.book.auth.domain.datasource.AccountDataSource
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.impl.passkey.createRelyingParty
import com.book.core.data.eventstreaming.StandardEventProducer
import com.book.core.data.eventstreaming.send
import com.book.core.domain.transaction.TransactionManager
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.event.UserEvents.DeleteUserEvent
import com.bookk.server.user.client.UserClient
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RegistrationResult
import com.yubico.webauthn.data.AuthenticatorAttestationResponse
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.exception.RegistrationFailedException
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

private typealias PKS = PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>

internal class FinishRegistrationImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val accountDataSource: AccountDataSource,
    private val deviceDataSource: DeviceDataSource,
    private val userClient: UserClient,
    private val generateAuthToken: GenerateAuthToken,
    private val credentialRepository: CredentialRepository,
    private val eventProducer: StandardEventProducer,
    private val transactionManager: TransactionManager
) : FinishRegistration {

    override suspend fun invoke(request: VerifyAccountCreationRequest) = runCatching {
        val pkc = PublicKeyCredential.parseRegistrationResponseJson(request.publicKeyCredentialJson)
        val challengeJson = passKeyDataSource.getCachedChallenge(request.userInfo.userId)
        if (challengeJson == null) throw FinishRegistration.Error.ChallengeWindowExpired
        val challenge = PublicKeyCredentialCreationOptions.fromJson(challengeJson)
        val result = validateRegistrationChallenge(request, challenge, pkc)
        val userId = saveUserExternal(request)
        transactionManager.runInTransaction {
            val ownerId = saveAuthorizationOwner(userId, request.userInfo)
            val passkeyIdentityId = passKeyDataSource.savePasskeyHandle(ownerId, challenge.user.id.bytes)
            savePasskeyCredentials(passkeyIdentityId, result, pkc)
            createAndSaveAuthCredentials(ownerId, request)
        }.onFailure {
            eventProducer.send(DeleteUserEvent(userId))
        }.getOrThrow()
    }.recoverCatching {
        when (it) {
            is RegistrationFailedException -> throw VerificationFailed
            is FinishRegistration.Error -> throw it
            else -> throw AccountCreationFailed
        }
    }

    private suspend fun createAndSaveAuthCredentials(ownerId: Long, request: VerifyAccountCreationRequest): AuthTokens {
        deviceDataSource.createDeviceIfNotExist(
            authId = ownerId,
            uuid = request.deviceInfo.deviceUUID,
            name = request.deviceInfo.deviceName
        )
        return generateAuthToken(Source.FromAuthDevice(ownerId, request.deviceInfo.deviceUUID)).getOrThrow()
    }

    private suspend fun saveUserExternal(request: VerifyAccountCreationRequest): Long {
        return userClient.createUser(createUserFrom(request.userInfo)).getOrThrow().id
    }

    private suspend fun saveAuthorizationOwner(userId: Long, userInfo: UserInfo): Long {
        val authentication = Authentication(
            id = 0,
            userId = userId,
            uuid = userInfo.userId
        )
        return accountDataSource.createAuthorization(authentication).id
    }

    private suspend fun savePasskeyCredentials(
        identityId: Long,
        registrationResult: RegistrationResult,
        publicCredential: PKS
    ) {
        val creds = registrationResult.asPasskeyCredential(identityId, publicCredential)
        passKeyDataSource.createPasskeyCredential(creds)
    }

    private suspend fun validateRegistrationChallenge(
        request: VerifyAccountCreationRequest,
        challenge: PublicKeyCredentialCreationOptions,
        response: PKS
    ): RegistrationResult {
        passKeyDataSource.deleteCachedChallenge(request.userInfo.userId)
        return createRelyingParty(credentialRepository).finishRegistration(
            FinishRegistrationOptions.builder()
                .request(challenge)
                .response(response)
                .build()
        )
    }

    @Suppress("DEPRECATION")
    private fun RegistrationResult.asPasskeyCredential(identityId: Long, pkc: PKS): PasskeyCredential {
        return PasskeyCredential(
            id = 0,
            userIdentityId = identityId,
            authInfo = Authentication(0, 0, ""), //Ignored
            handle = ByteArray(0), //Ignored
            credDescriptor = CredentialDescriptor(
                id = keyId.id.bytes,
                type = keyId.type.id,
                transports = keyId.transports.getOrNull()?.map { it.id }.orEmpty().toSet()
            ),
            publicKey = publicKeyCose.bytes,
            signatureCount = signatureCount,
            isDiscoverable = isDiscoverable.getOrElse { false },
            isBackupEligible = isBackupEligible,
            isBackedUp = isBackedUp,
            attestationObject = pkc.response.attestationObject.bytes,
            clientData = pkc.response.clientDataJSON.bytes
        )
    }

    private fun createUserFrom(info: UserInfo): User {
        return User(
            id = 0,
            name = info.name,
            lastName = info.lastName,
            email = info.email
        )
    }
}