package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.DeleteAccountInfo
import com.book.core.domain.entity.BusinessError

interface DeleteAccount {

    suspend operator fun invoke(userId: Long, info: DeleteAccountInfo): Result<Unit>

    sealed interface Error {
        data object InvalidCredentials : BusinessError(
            statusCode = 422,
            code = 1,
            message = "Invalid credentials"
        ), Error
    }
}