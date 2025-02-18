package com.book.auth.domain.datasource

import com.book.auth.domain.api.authentication.entity.Authentication

interface AccountDataSource {
    suspend fun createAuthorization(info: Authentication): Authentication
    suspend fun getAuthRecordById(id: Long): Authentication?
    suspend fun getAuthRecordByEmail(email: String): Authentication?
    suspend fun getAuthRecordByUserId(userId: Long): Authentication?
    suspend fun setNewEmail(authId: Long, newEmail: String)
    suspend fun deleteAuthorization(authId: Long)
}