package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.datasource.PassKeyDataSource
import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.entity.Authentication
import com.book.auth.domain.api.entity.PasskeyCredential
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest.UserInfo
import com.book.auth.domain.api.operation.FinishRegistration
import com.book.auth.domain.api.operation.FinishRegistration.Error.AccountCreationFailed
import com.book.auth.domain.api.operation.FinishRegistration.Error.VerificationFailed
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.GenerateAuthToken.Source
import com.book.user.domain.api.entity.User
import com.bookk.core.AppLevelConstants
import com.bookk.server.user.client.UserClient
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RegistrationResult
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.AuthenticatorAttestationResponse
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.RelyingPartyIdentity
import com.yubico.webauthn.exception.RegistrationFailedException
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

private typealias PKS = PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>

internal class FinishRegistrationImpl(
    private val passKeyDataSource: PassKeyDataSource,
    private val authDataSource: UserAuthDataSource,
    private val userClient: UserClient,
    private val generateAuthToken: GenerateAuthToken,
    credentialRepository: CredentialRepository
) : FinishRegistration {

    private val rp: RelyingParty = RelyingParty.builder()
        .identity(
            RelyingPartyIdentity.builder()
                .id(AppLevelConstants.DOMAIN_NAME)
                .name(AppLevelConstants.APP_NAME)
                .build()
        )
        .credentialRepository(credentialRepository)
        .build()

    override suspend fun invoke(request: VerifyAccountCreationRequest) = runCatching {
        val pkc = PublicKeyCredential.parseRegistrationResponseJson(request.publicKeyCredentialJson)
        val challengeJson = passKeyDataSource.getCachedChallenge(pkc.id.base64)
        val result: RegistrationResult = rp.finishRegistration(
            FinishRegistrationOptions.builder()
                .request(PublicKeyCredentialCreationOptions.fromJson(challengeJson))
                .response(pkc)
                .build()
        )
        passKeyDataSource.deleteCredentialOptions(pkc.id.base64)
        val userId = userClient.createUser(createUserFrom(request.userInfo)).getOrThrow()
        val auth = authDataSource.createAuthorization(createAuthRecordFrom(userId, request.userInfo))
        passKeyDataSource.createPasskeyCredential(result.asPasskeyCredential(auth.id, pkc))
        val device = authDataSource.createDevice(auth.id, request.deviceInfo.deviceUUID, request.deviceInfo.deviceName)
        generateAuthToken(Source.FromDeviceUUID(device.deviceInfo.deviceUUID)).getOrThrow()
    }.recoverCatching {
        when (it) {
            is RegistrationFailedException -> throw VerificationFailed
            else -> throw AccountCreationFailed
        }
    }

    private fun RegistrationResult.asPasskeyCredential(authId: Long, pkc: PKS): PasskeyCredential {
        val transports = keyId.transports.getOrNull()?.joinToString { it.id }.orEmpty()
        return PasskeyCredential(
            id = 0,
            authId = authId,
            userHandle = pkc.id.bytes,
            credDescriptorId = keyId.id.bytes,
            credDescriptorType = keyId.type.id,
            credDescriptorTransports = transports,
            publicKey = publicKeyCose.base64,
            signatureCount = signatureCount,
            isDiscoverable = isDiscoverable.getOrElse { false },
            isBackupEligible = isBackupEligible,
            isBackedUp = isBackedUp,
            attestationObject = pkc.response.attestationObject.bytes,
            clientData = pkc.response.clientDataJSON.bytes
        )
    }

    private fun createAuthRecordFrom(userId: Long, info: UserInfo): Authentication {
        return Authentication(
            id = 0,
            userId = userId,
            email = info.email
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