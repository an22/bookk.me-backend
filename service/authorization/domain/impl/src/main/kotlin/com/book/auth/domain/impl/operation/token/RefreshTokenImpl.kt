package com.book.auth.domain.impl.operation.token

import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.entity.RefreshTokenInfo
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.book.auth.domain.api.token.operation.RefreshToken
import com.book.auth.domain.api.token.operation.RefreshToken.Error.InvalidRefreshToken
import com.book.auth.domain.datasource.AccountDataSource

internal class RefreshTokenImpl(
    private val generateAuthToken: GenerateAuthToken,
    private val accountDataSource: AccountDataSource
) : RefreshToken {


    override suspend fun invoke(info: RefreshTokenInfo): Result<AuthTokens> = runCatching {
        if (info.refreshToken.isBlank()) throw InvalidRefreshToken
        val authRecord = accountDataSource.getAuthRecordByUserId(info.userId)
        if (authRecord != null) throw InvalidRefreshToken
        generateAuthToken(Source.FromRefresh(info.userId, info.refreshToken)).getOrThrow()
    }
}