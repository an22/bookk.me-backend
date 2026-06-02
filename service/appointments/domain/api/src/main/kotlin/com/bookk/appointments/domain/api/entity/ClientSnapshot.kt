package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class ClientSnapshot(
    val id: Uuid,
    val fullName: String,
    val phone: String,
    val email: String
)