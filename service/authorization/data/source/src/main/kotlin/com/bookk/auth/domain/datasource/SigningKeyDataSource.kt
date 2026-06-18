package com.bookk.auth.domain.datasource

import com.bookk.auth.domain.api.token.entity.SigningKey
import com.bookk.auth.domain.api.token.entity.SigningKeyStatus
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface SigningKeyDataSource {
    suspend fun getActiveKey(): SigningKey?
    suspend fun getVerificationKeys(): List<SigningKey>
    suspend fun insertKey(publicKeyPem: String, privateKeyPem: String): SigningKey
    suspend fun updateStatus(id: Uuid, status: SigningKeyStatus, retiredAt: Instant? = null)
    suspend fun deleteRetiredBefore(threshold: Instant)
}
