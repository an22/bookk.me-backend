package com.bookk.auth.domain.impl.operation.token

import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Error.InvalidCredentials
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.bookk.auth.domain.api.token.operation.RefreshToken

internal class RefreshTokenImpl(
    private val generateAuthToken: GenerateAuthToken
) : RefreshToken {

    override suspend fun invoke(token: String): Result<AuthTokens> {
        val (tokenId, secret) = OpaqueRefreshToken.parse(token) ?: return Result.failure(InvalidCredentials())
        return generateAuthToken(Source.FromRefresh(tokenId, secret))
    }
}