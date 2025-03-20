package com.book.auth.domain.api.delete_account.operation

import com.book.auth.domain.api.authentication.entity.FinishAssertionRequest
import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface DeleteAccount {

    suspend operator fun invoke(userId: Long, request: FinishAssertionRequest): Result<Unit>

    sealed interface Error {
        data object InvalidCredentials : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AuthErrorCodes.VERIFICATION_FAILED,
            message = "Invalid credentials"
        ), Error
    }
}