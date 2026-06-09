package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ClientSnapshot(
    val id: Uuid,
    val fullName: String,
    val phone: String,
    val email: String
) {
    companion object {
        fun stub() = ClientSnapshot(
            id = Uuid.random(),
            fullName = "Client Name",
            phone = "123456789",
            email = "client@example.com"
        )
    }
}