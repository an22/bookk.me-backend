package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.uuid.Uuid

@Serializable
data class AppointmentSettingsUpdate(
    @ProtoNumber(1) val businessId: Uuid,
    @ProtoNumber(2) val automaticApproval: Boolean,
    @ProtoNumber(3) val inBetweenBreakInMinutes: Int,
    @ProtoNumber(4) val appointmentNote: String
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
