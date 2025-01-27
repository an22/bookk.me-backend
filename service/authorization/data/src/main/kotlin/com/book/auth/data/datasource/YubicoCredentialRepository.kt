package com.book.auth.data.datasource

import com.book.auth.data.map.asPublicKeyCredentialDescriptor
import com.book.auth.data.map.asRegisteredCredential
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.datasource.PassKeyDataSource
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
            passKeyDataSource.getCredentialsByEmail(username)
                .map(PasskeyCredential::asPublicKeyCredentialDescriptor)
                .toMutableSet()
        }
    }

    override fun getUserHandleForUsername(username: String): Optional<YubicoByteArray> {
        return runBlocking {
            Optional.ofNullable(passKeyDataSource.getHandleByEmail(username)?.let { YubicoByteArray(it) })
        }
    }

    override fun getUsernameForUserHandle(handle: YubicoByteArray): Optional<String> {
        return runBlocking {
            Optional.ofNullable(passKeyDataSource.getEmailByHandle(handle.bytes))
        }
    }

    override fun lookup(credentialId: YubicoByteArray, handle: YubicoByteArray): Optional<RegisteredCredential> {
        return runBlocking {
            Optional.ofNullable(
                passKeyDataSource.getCredentialBy(handle.bytes, credentialId.bytes)?.asRegisteredCredential()
            )
        }
    }

    override fun lookupAll(credentialId: YubicoByteArray): MutableSet<RegisteredCredential> {
        return mutableSetOf()
    }
}