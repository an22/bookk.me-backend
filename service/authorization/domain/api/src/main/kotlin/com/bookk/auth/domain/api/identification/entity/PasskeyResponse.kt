package com.bookk.auth.domain.api.identification.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
class PasskeyResponse(
    val id: Uuid,
    val name: String,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val isBackedUp: Boolean
)