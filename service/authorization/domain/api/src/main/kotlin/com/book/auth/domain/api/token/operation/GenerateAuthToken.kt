package com.book.auth.domain.api.token.operation

import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.core.domain.entity.BusinessError

interface GenerateAuthToken {

    suspend operator fun invoke(source: Source): Result<AuthTokens>

    sealed interface Source {
        class FromRefresh(val userId: Long, val refreshToken: String) : Source
        class FromAuthDevice(val authId: Long, val deviceUUID: String) : Source
    }

    sealed interface Error {
        data object InvalidCredentials : BusinessError(422, 1, "Invalid credentials"), Error
    }
}