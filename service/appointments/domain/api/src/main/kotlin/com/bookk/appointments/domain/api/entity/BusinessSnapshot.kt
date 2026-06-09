package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class BusinessSnapshot(
    val id: Uuid,
    val name: String,
    val address: String,
    val isEnabled: Boolean
) {
    companion object {
        fun stub() = BusinessSnapshot(
            id = Uuid.random(),
            name = "Business name",
            address = "Business address",
            isEnabled = true
        )
    }
}