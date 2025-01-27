package com.book.auth.domain.api.authentication.operation

import com.book.auth.domain.api.authentication.entity.VerifySignInRequest
import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.core.domain.entity.BusinessError

interface FinishSignIn {
    suspend operator fun invoke(request: VerifySignInRequest): Result<AuthTokens>

    sealed interface Error {
        data object PasskeyOwnerNotFound : BusinessError(422, AuthErrorCodes.PASSKEY_OWNER_NOT_FOUND, "Passkey owner not found"), Error
        data object ChallengeWindowExpired : BusinessError(422, AuthErrorCodes.CHALLENGE_WINDOW_EXPIRED, "Challenge window expired"), Error
        data object VerificationFailed : BusinessError(422, AuthErrorCodes.VERIFICATION_FAILED, "Passkey verification failed"), Error
    }
}