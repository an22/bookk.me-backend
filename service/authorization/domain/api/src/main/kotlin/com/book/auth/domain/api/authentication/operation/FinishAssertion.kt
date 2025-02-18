package com.book.auth.domain.api.authentication.operation

import com.book.auth.domain.api.authentication.entity.FinishAssertionRequest
import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface FinishAssertion {

    suspend operator fun invoke(request: FinishAssertionRequest): Result<PasskeyCredential>

    sealed interface Error {
        data object PasskeyOwnerNotFound : BusinessError(HttpStatusCode.UnprocessableEntity.value, AuthErrorCodes.PASSKEY_OWNER_NOT_FOUND, "Passkey owner not found"), Error
        data object ChallengeWindowExpired : BusinessError(HttpStatusCode.UnprocessableEntity.value, AuthErrorCodes.CHALLENGE_WINDOW_EXPIRED, "Challenge window expired"), Error
        data object VerificationFailed : BusinessError(HttpStatusCode.UnprocessableEntity.value, AuthErrorCodes.VERIFICATION_FAILED, "Passkey verification failed"), Error
    }
}