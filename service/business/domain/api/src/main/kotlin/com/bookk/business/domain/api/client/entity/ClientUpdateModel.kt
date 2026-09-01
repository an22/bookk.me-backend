package com.bookk.business.domain.api.client.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ClientUpdateModel(
    val id: Uuid,
    val name: String?,
    val lastName: String?,
    val phone: String?,
    val email: String?,
    val description: String?
)
