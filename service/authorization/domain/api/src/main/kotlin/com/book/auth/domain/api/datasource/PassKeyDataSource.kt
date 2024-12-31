package com.book.auth.domain.api.datasource

interface PassKeyDataSource {
    suspend fun saveCredentialOptions(base64Handle: String, options: String)
    suspend fun getCredentialOptions(base64Handle: String): String?
    suspend fun deleteCredentialOptions(base64Handle: String)
}