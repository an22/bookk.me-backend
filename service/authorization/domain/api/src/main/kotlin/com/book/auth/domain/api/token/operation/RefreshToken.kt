package com.book.auth.domain.api.token.operation

import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.entity.RefreshTokenInfo
import com.book.core.domain.entity.BusinessError

interface RefreshToken {

    suspend operator fun invoke(info: RefreshTokenInfo): Result<AuthTokens>

    sealed interface Error {
        data object InvalidRefreshToken : BusinessError(422, 1, "Invalid refresh token")
    }
}