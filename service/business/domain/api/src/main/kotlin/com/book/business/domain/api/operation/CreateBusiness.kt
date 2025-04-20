package com.book.business.domain.api.operation

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.error.BusinessErrorCodes
import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface CreateBusiness {
    suspend operator fun invoke(userId: Long, name: String): Result<Business>

    sealed interface Error {
        data object BusinessExist : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_ALREADY_EXIST,
            message = "Business already exist"
        )

        data object BusinessValidationError : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_NAME_VALIDATION_ERROR,
            message = "Business name invalid"
        )
    }
}