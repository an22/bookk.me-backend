package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface MarkAppointmentNoShow {
    suspend operator fun invoke(userId: Uuid, appointmentId: Uuid): Result<Appointment>

    sealed interface Error {
        class AlreadyCancelled : Error, BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.APPOINTMENT_ALREADY_CANCELED,
            message = "Appointment already cancelled",
        )

        class NotStarted : Error, BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.APPOINTMENT_NOT_STARTED,
            message = "Appointment has not started yet",
        )
    }
}
