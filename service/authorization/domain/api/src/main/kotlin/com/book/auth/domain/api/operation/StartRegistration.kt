package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.ChallengeResponse
import com.book.auth.domain.api.entity.CreateAccountRequest
import com.book.core.domain.entity.BusinessError

interface StartRegistration {

    suspend operator fun invoke(request: CreateAccountRequest): Result<ChallengeResponse>

    sealed interface Error {
        data object EmailAlreadyExist : BusinessError(
            statusCode = 422,
            code = 1,
            message = "This email already exists"
        ), Error

        data object InvalidEmailFormat : BusinessError(
            statusCode = 422,
            code = 2,
            message = "Invalid email format"
        ), Error
    }
}