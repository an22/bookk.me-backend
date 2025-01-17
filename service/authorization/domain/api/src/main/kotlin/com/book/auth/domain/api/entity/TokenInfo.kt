package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)