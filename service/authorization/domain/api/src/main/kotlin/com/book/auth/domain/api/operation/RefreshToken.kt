package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.RefreshTokenInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.core.domain.entity.BusinessError

interface RefreshToken {

    suspend operator fun invoke(info: RefreshTokenInfo): Result<TokenInfo>

    sealed class RefreshTokenError(code: Int, message: String) : BusinessError(code, message) {
        data object InvalidRefreshToken : RefreshTokenError(1, "Invalid refresh token")
    }
}