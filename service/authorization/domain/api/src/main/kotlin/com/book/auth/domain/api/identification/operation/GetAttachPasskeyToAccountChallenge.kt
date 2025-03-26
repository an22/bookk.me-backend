package com.book.auth.domain.api.identification.operation

import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.registration.entity.RegistrationChallengeResponse
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface GetAttachPasskeyToAccountChallenge {
    suspend operator fun invoke(authId: Long, deviceId: Long, userId: Long): Result<RegistrationChallengeResponse>

    sealed interface Error {
        data object UnableToGeneratePasskeyChallenge : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AuthErrorCodes.UNABLE_TO_GENERATE_REGISTRATION_CHALLENGE,
            message = "Unable to provide registration challenge"
        ), Error
    }
}