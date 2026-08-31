package com.bookk.business.domain.api.appointment.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AppointmentBookingContextRequest(
    val employeeId: Uuid,
    val clientId: Uuid,
    val serviceIds: List<Uuid>
) {
    companion object {
        fun stub(
            employeeId: Uuid = Uuid.random(),
            clientId: Uuid = Uuid.random(),
            serviceIds: List<Uuid> = listOf(Uuid.random())
        ) = AppointmentBookingContextRequest(employeeId = employeeId, clientId = clientId, serviceIds = serviceIds)
    }
}
