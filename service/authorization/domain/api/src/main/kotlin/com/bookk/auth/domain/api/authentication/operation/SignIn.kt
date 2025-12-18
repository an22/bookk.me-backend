package com.bookk.auth.domain.api.authentication.operation

import com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest
import com.bookk.auth.domain.api.token.entity.AuthTokens

interface SignIn {
    suspend operator fun invoke(request: VerifySignInRequest): Result<AuthTokens>
}