package com.book.auth.data.map

import com.book.auth.data.orm.entity.AuthDeviceEntity
import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.data.orm.entity.PasskeyCredentialEntity
import com.book.auth.domain.api.authentication.entity.Authentication
import com.book.auth.domain.api.identification.entity.Device
import com.book.auth.domain.api.identification.entity.DeviceInfo
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.api.identification.entity.PasskeyCredential.CredentialDescriptor
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.AuthenticatorTransport
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import com.yubico.webauthn.data.PublicKeyCredentialType
import com.yubico.webauthn.data.ByteArray as YubicoByteArray

internal fun AuthenticationEntity.toDomain(): Authentication {
    return Authentication(
        id = id.value,
        userId = userId,
        email = email
    )
}

internal fun AuthDeviceEntity.toDomain(): Device {
    return Device(
        authRecord = userAuth.toDomain(),
        deviceInfo = DeviceInfo(
            id = id.value,
            deviceUUID = deviceUUID,
            refreshTokenId = refreshTokenId,
            deviceName = deviceName,
            isSignedIn = isSignedIn
        )
    )
}

internal fun PasskeyCredentialEntity.toDomain(): PasskeyCredential {
    return PasskeyCredential(
        id = id.value,
        userIdentityId = identity.id.value,
        authInfo = identity.authentication.toDomain(),
        handle = identity.userHandle,
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
        clientData = clientData
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
        .userHandle(YubicoByteArray(handle))
        .publicKeyCose(YubicoByteArray(publicKey))
        .signatureCount(signatureCount)
        .backupEligible(isBackupEligible)
        .backupState(isBackedUp)
        .build()
}