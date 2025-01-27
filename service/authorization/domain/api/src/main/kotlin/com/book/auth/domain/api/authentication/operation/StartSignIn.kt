package com.book.auth.domain.api.authentication.operation

import com.book.auth.domain.api.authentication.entity.SignInStartResponse

interface StartSignIn {
    suspend operator fun invoke(): Result<SignInStartResponse>
}