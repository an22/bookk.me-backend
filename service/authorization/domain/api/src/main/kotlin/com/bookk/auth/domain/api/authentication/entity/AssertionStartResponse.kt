package com.bookk.auth.domain.api.authentication.entity

import kotlinx.serialization.Serializable

@Serializable
data class AssertionStartResponse(
    val requestId: String,
    val challengeJson: String
)