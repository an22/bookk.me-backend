package com.book.auth.data.repository

import com.book.auth.domain.api.datasource.PassKeyDataSource
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import com.book.core.data.cache.withTransaction
import kotlin.time.Duration.Companion.minutes

internal class PassKeyDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : PassKeyDataSource {
    override suspend fun saveCredentialOptions(base64Handle: String, options: String) {
        cacheClient.withTransaction<_, String> {
            set(base64Handle, options)
            setExpiration(base64Handle, 5.minutes)
        }
    }

    override suspend fun getCredentialOptions(base64Handle: String): String? {
        return cacheClient.get<_, String>(base64Handle)
    }

    override suspend fun deleteCredentialOptions(base64Handle: String) {
        cacheClient.delete(base64Handle)
    }
}