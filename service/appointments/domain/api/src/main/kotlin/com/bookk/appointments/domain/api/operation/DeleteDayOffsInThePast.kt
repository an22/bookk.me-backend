package com.bookk.appointments.domain.api.operation

interface DeleteDayOffsInThePast {
    suspend operator fun invoke(): Result<Unit>
}
