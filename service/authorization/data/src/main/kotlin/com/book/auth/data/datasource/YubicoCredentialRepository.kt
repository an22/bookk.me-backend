package com.book.auth.data.datasource

import com.book.auth.data.map.asPublicKeyCredentialDescriptor
import com.book.auth.data.map.asRegisteredCredential
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.datasource.PassKeyDataSource
import com.bookk.core.toUUID
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import kotlinx.coroutines.runBlocking
import java.util.Optional
import com.yubico.webauthn.data.ByteArray as YubicoByteArray


internal class YubicoCredentialRepository(
    private val passKeyDataSource: PassKeyDataSource
) : CredentialRepository {

    override fun getCredentialIdsForUsername(username: String): MutableSet<PublicKeyCredentialDescriptor> {
        return runBlocking {
            passKeyDataSource.getCredentialsByUsername(username.toUUID())
                .map(PasskeyCredential::asPublicKeyCredentialDescriptor)
                .toMutableSet()
        }
    }

    override fun getUserHandleForUsername(username: String): Optional<YubicoByteArray> {
        return runBlocking {
            Optional.ofNullable(passKeyDataSource.getHandleByUsername(username.toUUID())?.let { YubicoByteArray(it.toByteArray()) })
        }
    }

    override fun getUsernameForUserHandle(handle: YubicoByteArray): Optional<String> {
        return runBlocking {
            Optional.ofNullable(passKeyDataSource.getUsernameByHandle(handle.bytes.toUUID()))
        }
    }

    override fun lookup(credentialId: YubicoByteArray, handle: YubicoByteArray): Optional<RegisteredCredential> {
        return runBlocking {
            Optional.ofNullable(
                passKeyDataSource.getCredentialBy(handle.bytes.toUUID(), credentialId.bytes)?.asRegisteredCredential()
            )
        }
    }

    override fun lookupAll(credentialId: YubicoByteArray): MutableSet<RegisteredCredential> {
        return runBlocking { passKeyDataSource.getCredentialsByCredentialId(credentialId.bytes) }
            .map(PasskeyCredential::asRegisteredCredential)
            .toMutableSet()
    }
}