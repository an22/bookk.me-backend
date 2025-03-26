package com.book.auth.domain.api.registration.operation

import com.book.auth.domain.api.authentication.entity.FinishRegistrationRequest
import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface FinishPasskeyRegistration {
    suspend fun verifyRequest(request: FinishRegistrationRequest): Result<PasskeyCredential>
    suspend fun attachOwner(ownerId: Long, passkey: PasskeyCredential): Result<Unit>

    sealed interface Error {
        data object ChallengeWindowExpired : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AuthErrorCodes.CHALLENGE_WINDOW_EXPIRED,
            message = "Challenge window expired"
        ), Error

        data object VerificationFailed : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AuthErrorCodes.VERIFICATION_FAILED,
            message = "Passkey verification failed"
        ), Error
    }
}