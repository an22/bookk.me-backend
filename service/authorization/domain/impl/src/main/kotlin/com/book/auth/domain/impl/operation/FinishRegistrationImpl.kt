package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.PassKeyDataSource
import com.book.auth.domain.api.entity.PasskeySignUpInfo
import com.book.auth.domain.api.operation.FinishRegistration
import com.bookk.core.AppLevelConstants
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RegistrationResult
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.RelyingPartyIdentity

internal class FinishRegistrationImpl(
    private val passKeyDataSource: PassKeyDataSource,
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

    override suspend fun call(params: PasskeySignUpInfo): Result<Unit> = runCatching {
        val pkc = PublicKeyCredential.parseRegistrationResponseJson(params.publicKeyCredentialJson)
        val request = passKeyDataSource.getCredentialOptions(pkc.id.base64)
        val result: RegistrationResult = rp.finishRegistration(
            FinishRegistrationOptions.builder()
                .request(PublicKeyCredentialCreationOptions.fromJson(request))
                .response(pkc)
                .build()
        )
        passKeyDataSource.deleteCredentialOptions(pkc.id.base64)
    }
}