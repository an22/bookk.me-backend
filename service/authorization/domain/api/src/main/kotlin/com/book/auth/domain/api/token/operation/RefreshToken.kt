package com.book.auth.domain.api.token.operation

import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.entity.RefreshTokenInfo
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface RefreshToken {

    suspend operator fun invoke(info: RefreshTokenInfo): Result<AuthTokens>

    sealed interface Error {
        data object InvalidRefreshToken : BusinessError(HttpStatusCode.UnprocessableEntity.value, AuthErrorCodes.INVALID_CREDENTIALS, "Invalid refresh token")
    }
}