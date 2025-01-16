package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest
import com.book.core.domain.entity.BusinessError

interface FinishRegistration {

    suspend operator fun invoke(request: VerifyAccountCreationRequest): Result<TokenInfo>

    sealed interface Error {
        data object InvalidEmailFormat : BusinessError(422, 1, "InvalidEmailFormat"), Error
        data object UserAlreadyExist : BusinessError(422, 2, "User with this email already exist"), Error
        data object VerificationFailed : BusinessError(422, 3, "Passkey verification failed"), Error
        data object AccountCreationFailed : BusinessError(500, 4, "Error during account creation, try again later"), Error
    }
}