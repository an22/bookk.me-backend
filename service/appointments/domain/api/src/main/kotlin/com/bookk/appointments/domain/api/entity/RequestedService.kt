package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class RequestedService(
    val serviceId: Uuid,
    val count: Int
) {
    companion object {
        fun stub(
            serviceId: Uuid = Uuid.random(),
            count: Int = 1
        ) = RequestedService(serviceId = serviceId, count = count)
    }
}
