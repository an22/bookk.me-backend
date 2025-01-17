package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeResponse(
    val challenge: String,
    val displayName: String,
    val userId: String
)