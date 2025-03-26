package com.book.auth.domain.api.identification.operation

import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.identification.entity.DeletePasskeyRequest
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface DeletePasskey {
    suspend operator fun invoke(body: DeletePasskeyRequest): Result<Unit>

    sealed interface Error {
        data object LastPasskey : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AuthErrorCodes.LAST_PASSKEY,
            message = "Can't delete passkey if it's the only one registered."
        ), Error
    }
}