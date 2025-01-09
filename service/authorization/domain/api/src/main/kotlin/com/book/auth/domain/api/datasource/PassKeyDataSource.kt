package com.book.auth.domain.api.datasource

import com.book.auth.domain.api.entity.PasskeyCredential

interface PassKeyDataSource {
    suspend fun saveChallengeToCache(base64Handle: String, options: String)
    suspend fun getCachedChallenge(base64Handle: String): String?
    suspend fun deleteCredentialOptions(base64Handle: String)
    suspend fun createPasskeyCredential(credential: PasskeyCredential)
}