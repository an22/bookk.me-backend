package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class EmployeeSnapshot(
    val id: Uuid,
    val fullName: String
) {
    companion object {
        fun stub(id: Uuid = Uuid.random()): EmployeeSnapshot {
            return EmployeeSnapshot(id = id, fullName = "Example name")
        }
    }
}