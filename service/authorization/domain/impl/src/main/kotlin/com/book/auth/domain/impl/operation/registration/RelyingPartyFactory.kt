package com.book.auth.domain.impl.operation.registration

import com.bookk.core.AppLevelConstants
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.RelyingPartyIdentity

internal fun createRelyingParty(credentialRepository: CredentialRepository) = RelyingParty.builder()
    .identity(
        RelyingPartyIdentity.builder()
            .id(AppLevelConstants.DOMAIN_NAME)
            .name(AppLevelConstants.APP_NAME)
            .build()
    )
    .credentialRepository(credentialRepository)
    .origins(
        setOf(
            "android:apk-key-hash:Vi-agbMa0y87EKsc03SQkkX9AukAW3YWZ-b1i8ba7Cs",
            "android:apk-key-hash:WAnYmsiy9ZHg_jsg71y6Gb9ldVaYnQGsKHXowwwyqfY"
        )
    )
    .build()