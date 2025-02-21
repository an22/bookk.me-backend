package com.book.auth.domain.api.token.operation

import com.book.auth.domain.api.error.AuthErrorCodes.INVALID_CREDENTIALS
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface GenerateAuthToken {

    suspend operator fun invoke(source: Source): Result<AuthTokens>

    sealed interface Source {
        class FromRefresh(val deviceId: Long, val tokenId: String) : Source
        class FromAuthDevice(val authId: Long, val deviceUUID: String) : Source
    }

    sealed interface Error {
        data object InvalidCredentials : BusinessError(
            statusCode = HttpStatusCode.Unauthorized.value,
            code = INVALID_CREDENTIALS,
            message = "Invalid credentials"
        ), Error
    }
}