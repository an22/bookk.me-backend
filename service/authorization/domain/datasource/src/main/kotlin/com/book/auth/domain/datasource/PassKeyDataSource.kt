package com.book.auth.domain.datasource

import com.book.auth.domain.api.identification.entity.PasskeyCredential

interface PassKeyDataSource {
    suspend fun saveChallengeToCache(requestId: String, challenge: String)
    suspend fun getCachedChallenge(requestId: String): String?
    suspend fun deleteCachedChallenge(requestId: String)
    suspend fun createPasskeyCredential(credential: PasskeyCredential)
    suspend fun getCredentialBy(userHandle: ByteArray, credentialId: ByteArray): PasskeyCredential?
    suspend fun getCredentialBy(authId: Long): List<PasskeyCredential>
    suspend fun getHandleByUsername(username: String): ByteArray?
    suspend fun getUsernameByHandle(userHandle: ByteArray): String?
    suspend fun getCredentialsByUsername(username: String): Set<PasskeyCredential>
    suspend fun getCredentialsByCredentialId(credentialId: ByteArray): Set<PasskeyCredential>
    suspend fun markAsUsed(passkeyCredentialId: Long)
}