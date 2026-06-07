package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import kotlin.uuid.Uuid

interface EnableAppointmentsForBusiness {
    suspend operator fun invoke(userId: Uuid, snapshot: BusinessSnapshot): Result<Unit>
}