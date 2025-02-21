package com.book.auth.domain.impl.operation.token

import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.entity.RefreshTokenInfo
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.book.auth.domain.api.token.operation.RefreshToken
import com.book.auth.domain.api.token.operation.RefreshToken.Error.InvalidRefreshToken

internal class RefreshTokenImpl(
    private val generateAuthToken: GenerateAuthToken
) : RefreshToken {

    override suspend fun invoke(info: RefreshTokenInfo): Result<AuthTokens> = runCatching {
        if (info.tokenId.isBlank()) throw InvalidRefreshToken
        generateAuthToken(Source.FromRefresh(info.deviceId, info.tokenId)).getOrThrow()
    }
}