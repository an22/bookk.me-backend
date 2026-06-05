package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface CreateAppointmentRequest {
    suspend operator fun invoke(userId: Uuid, request: AppointmentRequest): Result<Unit>

    sealed interface Error {
        class RequestForThisTimeExists : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.REQUEST_EXISTS,
            message = "Request for this time already exists"
        ), Error

        class RequestForThisTimeNotAllowed : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.REQUEST_EXISTS,
            message = "Request for this time already exists"
        ), Error

        class RequestForThisDateNotAllowed : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.TIME_NOT_ALLOWED,
            message = "Request for this date and time not allowed"
        ), Error
    }
}