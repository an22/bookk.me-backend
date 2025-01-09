package com.book.auth.domain.impl.operation.token

import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.entity.RefreshTokenInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.GenerateAuthToken.Source
import com.book.auth.domain.api.operation.RefreshToken
import com.book.auth.domain.api.operation.RefreshToken.RefreshTokenError

internal class RefreshTokenImpl(
    private val generateAuthToken: GenerateAuthToken,
    private val localDataSource: UserAuthDataSource
) : RefreshToken {


    override suspend fun invoke(info: RefreshTokenInfo): Result<TokenInfo> = runCatching {
        if (info.refreshToken.isBlank()) throw RefreshTokenError.InvalidRefreshToken
        val authRecord = localDataSource.getAuthRecordByUserId(info.userId)
        if (authRecord != null) throw RefreshTokenError.InvalidRefreshToken
        generateAuthToken(Source.FromRefresh(info.userId, info.refreshToken)).getOrThrow()
    }
}