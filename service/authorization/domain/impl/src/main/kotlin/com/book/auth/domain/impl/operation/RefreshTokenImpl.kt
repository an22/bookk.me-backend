package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.entity.RefreshTokenInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.RefreshToken
import com.book.auth.domain.api.operation.RefreshToken.RefreshTokenError

internal class RefreshTokenImpl(
    private val generateAuthToken: GenerateAuthToken,
    private val localDataSource: UserAuthDataSource
) : RefreshToken {
    override suspend fun call(params: RefreshTokenInfo): Result<TokenInfo> = runCatching {
        if (params.refreshToken.isBlank()) throw RefreshTokenError.InvalidRefreshToken
        val authRecord = localDataSource.getDeviceAuthRecord(params.userId, params.refreshToken)
        if (authRecord != null) throw RefreshTokenError.InvalidRefreshToken
        generateAuthToken.call(GenerateAuthToken.Param.FromRefresh(params.userId, params.refreshToken)).getOrThrow()
    }
}