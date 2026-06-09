package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface DeclineAppointmentRequest {
    suspend operator fun invoke(userId: Uuid, cancellation: AppointmentCancellation): Result<Unit>

    sealed interface Error {
        class AlreadyDeclined: Error, BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.REQUEST_ALREADY_DECLINED,
            message = "Appointment request already declined",
        )

        class AlreadyApproved: Error, BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.REQUEST_ALREADY_APPROVED,
            message = "Appointment request already approved",
        )
    }
}