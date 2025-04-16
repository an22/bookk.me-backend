package com.book.auth.domain.api.identification.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class PasskeyResponse(
    val id: Long,
    val name: String,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val isBackedUp: Boolean
)