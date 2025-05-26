package com.book.business.domain.api.operation

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.error.BusinessErrorCodes
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface GetBusinessById {
    suspend operator fun invoke(id: Long): Result<Business>

    sealed interface Error {
        data object NotFound : BusinessError(
            statusCode = HttpStatusCode.NotFound.value,
            code = BusinessErrorCodes.BUSINESS_NOT_FOUND,
            message = "Business with this id is missing"
        )
    }
}