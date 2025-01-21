package com.book.auth.domain.api.authentication.entity

import kotlinx.serialization.Serializable

@Serializable
data class SignInStartResponse(
    val requestId: String,
    val challengeJson: String
)