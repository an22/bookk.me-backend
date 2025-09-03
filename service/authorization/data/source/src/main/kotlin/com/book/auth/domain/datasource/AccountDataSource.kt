package com.book.auth.domain.datasource

import com.book.auth.domain.api.authentication.entity.Authentication
import kotlin.uuid.Uuid

interface AccountDataSource {
    suspend fun createAuthorization(info: Authentication): Authentication
    suspend fun getAuthRecordById(id: Uuid): Authentication?
    suspend fun getAuthRecordByUUID(uuid: Uuid): Authentication?
    suspend fun getAuthRecordByUserId(userId: Uuid): Authentication?
    suspend fun deleteAuthorization(authId: Uuid)
}