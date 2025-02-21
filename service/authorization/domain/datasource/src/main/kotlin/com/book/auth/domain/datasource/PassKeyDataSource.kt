package com.book.auth.domain.datasource

import com.book.auth.domain.api.identification.entity.PasskeyCredential

interface PassKeyDataSource {
    suspend fun saveChallengeToCache(requestId: String, challenge: String)
    suspend fun getCachedChallenge(requestId: String): String?
    suspend fun deleteCachedChallenge(requestId: String)

    suspend fun createPasskeyCredential(credential: PasskeyCredential)
    suspend fun getCredentialBy(userHandle: ByteArray, credentialId: ByteArray): PasskeyCredential?
    suspend fun getUUIDByHandle(userHandle: ByteArray): String?
    suspend fun getCredentialsByUUID(uuid: String): Set<PasskeyCredential>

    suspend fun getHandleByUUID(uuid: String): ByteArray?
    suspend fun savePasskeyHandle(authenticationId: Long, handle: ByteArray): Long
}