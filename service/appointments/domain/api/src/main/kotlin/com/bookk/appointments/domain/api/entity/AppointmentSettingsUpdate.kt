package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AppointmentSettingsUpdate(
    val businessId: Uuid,
    val automaticApproval: Boolean,
    val inBetweenBreakInMinutes: Int,
    val appointmentNote: String
) {
    companion object {
        fun stub(
            businessId: Uuid = Uuid.random(),
            automaticApproval: Boolean = false,
            inBetweenBreakInMinutes: Int = 10,
            appointmentNote: String = ""
        ) = AppointmentSettingsUpdate(
            businessId = businessId,
            automaticApproval = automaticApproval,
            inBetweenBreakInMinutes = inBetweenBreakInMinutes,
            appointmentNote = appointmentNote
        )
    }
}
