package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.entity.Authentication
import com.book.auth.domain.api.entity.PasskeyCredential
import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest.UserInfo
import com.book.auth.domain.api.operation.FinishRegistration
import com.book.auth.domain.api.operation.FinishRegistration.Error.AccountCreationFailed
import com.book.auth.domain.api.operation.FinishRegistration.Error.UserAlreadyExist
import com.book.auth.domain.api.operation.FinishRegistration.Error.VerificationFailed
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.GenerateAuthToken.Source
import com.book.auth.domain.datasource.AccountDataSource
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.auth.domain.datasource.PassKeyDataSource
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
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.exception.RegistrationFailedException
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.error
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

    private val logger = KtorSimpleLogger("FinishRegistration")

    override suspend fun invoke(request: VerifyAccountCreationRequest) = runCatching {
        verifyUserDoesNotExist(request)
        val pkc = PublicKeyCredential.parseRegistrationResponseJson(request.publicKeyCredentialJson)
        val result = validateRegistrationChallenge(request, pkc)
        val userId = saveUserExternal(request)
        transactionManager.runInTransaction {
            val ownerId = saveAuthorizationOwner(userId, request.userInfo)
            throw Exception()
            savePasskeyCredentials(ownerId, request.userInfo.userId, result, pkc)
            createAndSaveAuthCredentials(ownerId, request)
        }.onFailure {
            eventProducer.send(DeleteUserEvent.TOPIC, DeleteUserEvent(userId))
        }.getOrThrow()
    }.recoverCatching {
        logger.error(it)
        when (it) {
            is RegistrationFailedException -> throw VerificationFailed
            is FinishRegistration.Error -> throw it
            else -> throw AccountCreationFailed
        }
    }

    private suspend fun createAndSaveAuthCredentials(ownerId: Long, request: VerifyAccountCreationRequest): TokenInfo {
        val device = deviceDataSource.createDevice(
            authId = ownerId,
            uuid = request.deviceInfo.deviceUUID,
            name = request.deviceInfo.deviceName
        )
        return generateAuthToken(Source.FromDeviceUUID(device.deviceInfo.deviceUUID)).getOrThrow()
    }

    private suspend fun verifyUserDoesNotExist(request: VerifyAccountCreationRequest) {
        if (accountDataSource.getAuthRecordByEmail(request.userInfo.email) != null) throw UserAlreadyExist
    }

    private suspend fun saveUserExternal(request: VerifyAccountCreationRequest): Long {
        return userClient.createUser(createUserFrom(request.userInfo)).getOrThrow()
    }

    private suspend fun saveAuthorizationOwner(userId: Long, userInfo: UserInfo): Long {
        val authentication = Authentication(
            id = 0,
            userId = userId,
            email = userInfo.email
        )
        return accountDataSource.createAuthorization(authentication).id
    }

    private suspend fun savePasskeyCredentials(
        ownerId: Long,
        userHandle: String,
        registrationResult: RegistrationResult,
        publicCredential: PKS
    ) {
        val creds = registrationResult.asPasskeyCredential(ownerId, userHandle, publicCredential)
        logger.info(creds.toString())
        passKeyDataSource.createPasskeyCredential(creds)
    }

    private suspend fun validateRegistrationChallenge(
        request: VerifyAccountCreationRequest,
        pkc: PKS
    ): RegistrationResult {
        val challengeJson = passKeyDataSource.getCachedChallenge(request.userInfo.userId)
        passKeyDataSource.deleteCredentialOptions(request.userInfo.userId)
        return createRelyingParty(credentialRepository).finishRegistration(
            FinishRegistrationOptions.builder()
                .request(PublicKeyCredentialCreationOptions.fromJson(challengeJson))
                .response(pkc)
                .build()
        )
    }

    @Suppress("DEPRECATION")
    private fun RegistrationResult.asPasskeyCredential(authId: Long, userId: String, pkc: PKS): PasskeyCredential {
        logger.debug(toString())
        val transports = keyId.transports.getOrNull()?.joinToString { it.id }.orEmpty()
        return PasskeyCredential(
            id = 0,
            authId = authId,
            userHandle = ByteArray.fromBase64(userId).bytes,
            credDescriptorId = keyId.id.bytes,
            credDescriptorType = keyId.type.id,
            credDescriptorTransports = transports,
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
            email = info.email,
            phone = null
        )
    }
}