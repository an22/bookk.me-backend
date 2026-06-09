package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface CancelAppointment {
    suspend operator fun invoke(userId: Uuid, cancellation: AppointmentCancellation): Result<Unit>

    sealed interface Error {
        class AlreadyCancelled: Error, BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.APPOINTMENT_ALREADY_CANCELED,
            message = "Appointment already cancelled",
        )

        class AlreadyCompleted: Error, BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.APPOINTMENT_ALREADY_COMPLETED,
            message = "Appointment already cancelled",
        )
    }
}