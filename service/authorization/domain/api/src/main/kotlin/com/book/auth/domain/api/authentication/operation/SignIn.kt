package com.book.auth.domain.api.authentication.operation

import com.book.auth.domain.api.authentication.entity.VerifySignInRequest
import com.book.auth.domain.api.token.entity.AuthTokens

interface SignIn {
    suspend operator fun invoke(request: VerifySignInRequest): Result<AuthTokens>
}