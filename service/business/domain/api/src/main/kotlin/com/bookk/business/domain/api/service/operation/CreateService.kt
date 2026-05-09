package com.bookk.business.domain.api.service.operation

import com.bookk.business.domain.api.error.BusinessErrorCodes
import com.bookk.business.domain.api.service.entity.Service
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface CreateService {
    suspend operator fun invoke(service: Service): Result<Service>

    sealed interface Error {
        class ServiceExist : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_SERVICE_EXISTS,
            message = "Service with this name already exists"
        )

        class ValidationError : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_SERVICE_NAME_VALIDATION_ERROR,
            message = "Invalid service name"
        )
    }
}