package com.book.user.domain.api.operation

import com.book.core.domain.entity.BusinessError
import com.book.user.domain.api.entity.User

interface GetUserById {

    suspend operator fun invoke(userId: Long): Result<User>

    sealed interface GetCurrentUserError {
        data object UserNotFound : BusinessError(
            statusCode = 404,
            code = 1,
            message = "User not found"
        ), GetCurrentUserError
    }
}