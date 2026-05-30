package com.bookk.business.domain.api.service.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class ServiceGroup(
    val id: Uuid,
    val businessId: Uuid,
    val name: String,
    val createdAt: Instant
)