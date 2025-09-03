package com.book.auth.domain.api.token.operation

import com.book.auth.domain.api.error.AuthErrorCodes.INVALID_CREDENTIALS
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface GenerateAuthToken {

    suspend operator fun invoke(source: Source): Result<AuthTokens>

    sealed interface Source {
        class FromRefresh(val deviceId: Uuid, val tokenId: Uuid) : Source
        class FromAuthDevice(val authId: Uuid, val deviceUUID: Uuid) : Source
    }

    sealed interface Error {
        data object InvalidCredentials : BusinessError(
            statusCode = HttpStatusCode.Unauthorized.value,
            code = INVALID_CREDENTIALS,
            message = "Invalid credentials"
        ), Error
    }
}