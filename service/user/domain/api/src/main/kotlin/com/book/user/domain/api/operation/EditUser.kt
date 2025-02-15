package com.book.user.domain.api.operation

import com.book.core.domain.entity.BusinessError
import com.book.user.domain.api.entity.User
import io.ktor.http.HttpStatusCode

interface EditUser {
    suspend operator fun invoke(user: User): Result<Unit>

    sealed interface Error {
        data object UserNotFound : BusinessError(
            statusCode = HttpStatusCode.NotFound.value,
            code = 1,
            message = "User not found"
        ), Error
    }
}