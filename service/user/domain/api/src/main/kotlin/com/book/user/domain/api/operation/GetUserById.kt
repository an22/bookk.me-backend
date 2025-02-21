package com.book.user.domain.api.operation

import com.book.core.domain.entity.BusinessError
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.error.UserErrorCodes
import io.ktor.http.HttpStatusCode

interface GetUserById {

    suspend operator fun invoke(userId: Long): Result<User>

    sealed interface Error {
        data object UserNotFound : BusinessError(
            statusCode = HttpStatusCode.NotFound.value,
            code = UserErrorCodes.USER_NOT_EXIST,
            message = "User not found"
        ), Error
    }
}