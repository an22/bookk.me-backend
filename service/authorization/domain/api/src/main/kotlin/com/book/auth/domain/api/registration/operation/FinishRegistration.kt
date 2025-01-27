package com.book.auth.domain.api.registration.operation

import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.core.domain.entity.BusinessError

interface FinishRegistration {

    suspend operator fun invoke(request: VerifyAccountCreationRequest): Result<AuthTokens>

    sealed interface Error {
        data object InvalidEmailFormat : BusinessError(422, AuthErrorCodes.INVALID_EMAIL_FORMAT, "InvalidEmailFormat"), Error
        data object UserAlreadyExist : BusinessError(422, AuthErrorCodes.USER_ALREADY_EXIST, "User with this email already exist"), Error
        data object ChallengeWindowExpired : BusinessError(422, AuthErrorCodes.CHALLENGE_WINDOW_EXPIRED, "Challenge window expired"), Error
        data object VerificationFailed : BusinessError(422, AuthErrorCodes.VERIFICATION_FAILED, "Passkey verification failed"), Error
        data object AccountCreationFailed : BusinessError(500, AuthErrorCodes.ACCOUNT_CREATION_FAILED, "Error during account creation, try again later"), Error
    }
}