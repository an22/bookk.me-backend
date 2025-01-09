package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest

interface FinishRegistration {

    suspend operator fun invoke(request: VerifyAccountCreationRequest): Result<TokenInfo>

    sealed class Error : Exception() {
        data object VerificationFailed : Error()
        data object AccountCreationFailed : Error()
    }
}