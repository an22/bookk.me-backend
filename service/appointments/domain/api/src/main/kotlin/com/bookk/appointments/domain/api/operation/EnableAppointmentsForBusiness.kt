package com.bookk.appointments.domain.api.operation

import kotlin.uuid.Uuid

interface EnableAppointmentsForBusiness {
    suspend operator fun invoke(userId: Uuid, businessId: Uuid): Result<Unit>
}