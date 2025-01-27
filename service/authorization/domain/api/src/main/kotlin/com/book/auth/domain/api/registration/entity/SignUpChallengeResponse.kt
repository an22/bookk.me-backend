package com.book.auth.domain.api.registration.entity

import kotlinx.serialization.Serializable

@Serializable
data class SignUpChallengeResponse(
    val challenge: String,
    val displayName: String,
    val userId: String
)