package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AppointmentRequest(
    override val id: Uuid,
    override val userId: Uuid,
    override val businessId: Uuid,
    val employee: EmployeeSnapshot,
    val client: ClientSnapshot,
    val services: List<ServiceSnapshot>,
    val status: AppointmentRequestStatus,
    override val date: Instant,
    val note: String,
    val declineReason: String,
) : AppointmentRepresentation {

    @Transient
    override val dateEnd = date + services.fold(0.minutes) { acc, service ->
        acc + service.duration
    }

    @Transient
    val totalAmount = services.fold(services[0].price) { acc, service ->
        acc + service.price
    }

    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            date: Instant = Instant.fromEpochMilliseconds(0)
        ) = AppointmentRequest(
            id = id,
            userId = userId,
            businessId = businessId,
            employee = EmployeeSnapshot.stub(),
            client = ClientSnapshot.stub(),
            services = listOf(ServiceSnapshot.stub()),
            status = AppointmentRequestStatus.PENDING,
            date = date,
            note = "Note",
            declineReason = ""
        )
    }
}