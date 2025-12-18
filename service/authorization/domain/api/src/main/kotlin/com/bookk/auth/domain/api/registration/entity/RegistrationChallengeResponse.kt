package com.bookk.auth.domain.api.registration.entity

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationChallengeResponse(
    val requestId: String,
    val challenge: String,
    val displayName: String
)