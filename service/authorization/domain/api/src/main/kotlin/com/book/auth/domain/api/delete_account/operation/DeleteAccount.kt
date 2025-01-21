package com.book.auth.domain.api.delete_account.operation

import com.book.core.domain.entity.BusinessError

interface DeleteAccount {

    suspend operator fun invoke(userId: Long): Result<Unit>

    sealed interface Error {
        data object InvalidCredentials : BusinessError(
            statusCode = 422,
            code = 1,
            message = "Invalid credentials"
        ), Error
    }
}