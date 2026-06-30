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
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            name: String = "stub-group",
            createdAt: Instant = Instant.fromEpochMilliseconds(0)
        ) = ServiceGroup(id = id, businessId = businessId, name = name, createdAt = createdAt)
    }
}