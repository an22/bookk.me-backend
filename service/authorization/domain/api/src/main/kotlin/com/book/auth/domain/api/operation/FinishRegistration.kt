package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest
import com.book.core.domain.entity.BusinessError

interface FinishRegistration {

    suspend operator fun invoke(request: VerifyAccountCreationRequest): Result<TokenInfo>

    sealed class Error(code: Int, message: String) : BusinessError(code, errorMessage = message) {
        data object UserAlreadyExist : Error(1, "User with this email already exist")
        data object VerificationFailed : Error(2, "Passkey verification failed")
        data object AccountCreationFailed : Error(3, "Error during account creation, try again later")
    }
}