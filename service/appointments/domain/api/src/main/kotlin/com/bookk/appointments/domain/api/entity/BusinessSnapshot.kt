package com.bookk.appointments.domain.api.entity

import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import library.schedule.Schedule
import kotlin.uuid.Uuid

@Serializable
data class BusinessSnapshot(
    val id: Uuid,
    val name: String,
    val address: String,
    val timeZone: TimeZone,
    val isEnabled: Boolean,
    val schedule: Schedule
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            name: String = "Business name",
            address: String = "Business address",
            timeZone: TimeZone = TimeZone.UTC,
            isEnabled: Boolean = true,
            schedule: Schedule = Schedule()
        ) = BusinessSnapshot(
            id = id,
            name = name,
            address = address,
            timeZone = timeZone,
            isEnabled = isEnabled,
            schedule = schedule
        )
    }
}
