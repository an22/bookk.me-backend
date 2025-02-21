package com.book.auth.domain.api.registration.operation

import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.registration.entity.CreateAccountRequest
import com.book.auth.domain.api.registration.entity.SignUpChallengeResponse
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface StartRegistration {

    suspend operator fun invoke(request: CreateAccountRequest): Result<SignUpChallengeResponse>

    sealed interface Error {
        data object EmailAlreadyExist : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AuthErrorCodes.EMAIL_EXIST,
            message = "This email already exists"
        ), Error

        data object InvalidEmailFormat : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AuthErrorCodes.INVALID_EMAIL_FORMAT,
            message = "Invalid email format"
        ), Error
    }
}