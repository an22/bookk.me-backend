package com.bookk.business.domain.api.operation

import com.bookk.business.domain.api.entity.Client
import com.bookk.business.domain.api.entity.ClientRemote
import com.bookk.business.domain.api.error.BusinessErrorCodes
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface CreateClient {
    suspend operator fun invoke(businessId: Uuid, client: Client): Result<ClientRemote>

    sealed interface Error {
        data object ClientExist : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_CLIENT_EXISTS,
            message = "Client with this phone already exists"
        )

        data object ClientValidationError : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_CLIENT_NAME_VALIDATION_ERROR,
            message = "Client name or last name is too long"
        )
    }
}