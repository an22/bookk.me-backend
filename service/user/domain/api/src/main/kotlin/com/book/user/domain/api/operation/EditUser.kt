package com.book.user.domain.api.operation

import com.book.core.domain.entity.BusinessError
import com.book.user.domain.api.entity.UserEditModel
import com.book.user.domain.api.error.UserErrorCodes
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface EditUser {
    suspend operator fun invoke(id: Uuid, user: UserEditModel): Result<Unit>

    sealed interface Error {
        data object UserNotFound : BusinessError(
            statusCode = HttpStatusCode.NotFound.value,
            code = UserErrorCodes.USER_NOT_EXIST,
            message = "User not found"
        ), Error
    }
}