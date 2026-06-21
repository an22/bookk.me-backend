package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface EditSettings {
    suspend operator fun invoke(userId: Uuid, settings: AppointmentSettings): Result<Unit>

    sealed interface Error {
        class ActiveDayWithoutWorkHours : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.ACTIVE_DAY_WITHOUT_WORK_HOURS,
            message = "Active day must have at least one work hour"
        ), Error

        class InvalidDayOffRange : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = AppointmentErrorCodes.INVALID_DAY_OFF_RANGE,
            message = "Day off range start date must be before end date"
        ), Error
    }
}
