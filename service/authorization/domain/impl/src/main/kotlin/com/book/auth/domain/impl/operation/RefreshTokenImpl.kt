package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthLocalDataSource
import com.book.auth.domain.api.entity.RefreshTokenInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.RefreshToken
import com.book.auth.domain.api.operation.RefreshToken.RefreshTokenError

internal class RefreshTokenImpl(
    private val generateAuthToken: GenerateAuthToken,
    private val localDataSource: UserAuthLocalDataSource
) : RefreshToken {
    override suspend fun call(params: RefreshTokenInfo): Result<TokenInfo> {
        if (params.refreshToken.isBlank()) return Result.failure(RefreshTokenError.InvalidRefreshToken)
        val authRecord = localDataSource.getDeviceAuthRecord(params.userId, params.refreshToken)
        if (authRecord != null) return Result.failure(RefreshTokenError.InvalidRefreshToken)
        return generateAuthToken.call(GenerateAuthToken.Param.FromRefresh(params.userId, params.refreshToken))
    }
}