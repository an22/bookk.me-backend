package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.RefreshTokenInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.core.domain.entity.BusinessError

interface RefreshToken {

    suspend operator fun invoke(info: RefreshTokenInfo): Result<TokenInfo>

    sealed interface Error {
        data object InvalidRefreshToken : BusinessError(422, 1, "Invalid refresh token")
    }
}