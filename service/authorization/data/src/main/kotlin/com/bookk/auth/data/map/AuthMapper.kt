package com.bookk.auth.data.map

import com.bookk.auth.data.orm.entity.AuthDeviceEntity
import com.bookk.auth.data.orm.entity.AuthenticationEntity
import com.bookk.auth.data.orm.entity.PasskeyCredentialEntity
import com.bookk.auth.data.orm.entity.SigningKeyEntity
import com.bookk.auth.domain.api.authentication.entity.Authentication
import com.bookk.auth.domain.api.identification.entity.Device
import com.bookk.auth.domain.api.identification.entity.DeviceInfo
import com.bookk.auth.domain.api.identification.entity.PasskeyCredential
import com.bookk.auth.domain.api.identification.entity.PasskeyCredential.CredentialDescriptor
import com.bookk.auth.domain.api.token.entity.SafeRefreshToken
import com.bookk.auth.domain.api.token.entity.SigningKey
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.AuthenticatorTransport
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import com.yubico.webauthn.data.PublicKeyCredentialType
import kotlin.uuid.toKotlinUuid
import com.yubico.webauthn.data.ByteArray as YubicoByteArray

internal fun AuthenticationEntity.toDomain(): Authentication {
    return Authentication(
        id = id.value.toKotlinUuid(),
        userId = userId.toKotlinUuid(),
        uuid = uuid.toKotlinUuid()
    )
}

internal fun AuthDeviceEntity.toDomain(): Device {
    return Device(
        authRecord = userAuth.toDomain(),
        deviceInfo = DeviceInfo(
            id = id.value.toKotlinUuid(),
            deviceUUID = deviceUUID.toKotlinUuid(),
            refreshToken = SafeRefreshToken.from(refreshTokenId?.toKotlinUuid(), refreshTokenHash),
            previousRefreshToken = SafeRefreshToken.from(previousRefreshTokenId?.toKotlinUuid(), previousRefreshTokenHash),
            deviceName = deviceName,
            isSignedIn = isSignedIn,
        )
    )
}

internal fun PasskeyCredentialEntity.toDomain(): PasskeyCredential {
    return PasskeyCredential(
        id = id.value.toKotlinUuid(),
        authId = authorization.id.value.toKotlinUuid(),
        authInfo = authorization.toDomain(),
        handle = authorization.uuid.toKotlinUuid(),
        name = name,
        credDescriptor = CredentialDescriptor(
            id = credDescriptorId,
            type = credDescriptorType,
            transports = credDescriptorTransports.split(',').toSet()
        ),
        publicKey = publicKey,
        signatureCount = signatureCount,
        isDiscoverable = isDiscoverable,
        isBackupEligible = isBackupEligible,
        isBackedUp = isBackedUp,
        attestationObject = attestationObject,
        clientData = clientData,
        lastUsedAt = lastUsedAt,
        createdAt = createdAt
    )
}

internal fun SigningKeyEntity.toDomain(): SigningKey {
    return SigningKey(
        id = id.value.toKotlinUuid(),
        publicKeyPem = publicKey,
        privateKeyPem = privateKey,
        status = status,
        createdAt = createdAt,
        retiredAt = retiredAt
    )
}

internal fun PasskeyCredential.asPublicKeyCredentialDescriptor(): PublicKeyCredentialDescriptor {
    return PublicKeyCredentialDescriptor.builder()
        .id(YubicoByteArray(credDescriptor.id))
        .type(PublicKeyCredentialType.entries.first { it.id == credDescriptor.type })
        .transports(credDescriptor.transports.map { AuthenticatorTransport.of(it) }.toSet())
        .build()
}

@Suppress("DEPRECATION")
internal fun PasskeyCredential.asRegisteredCredential(): RegisteredCredential {
    return RegisteredCredential.builder()
        .credentialId(YubicoByteArray(credDescriptor.id))
        .userHandle(YubicoByteArray(handle.toByteArray()))
        .publicKeyCose(YubicoByteArray.fromBase64(publicKey))
        .signatureCount(signatureCount)
        .backupEligible(isBackupEligible)
        .backupState(isBackedUp)
        .build()
}