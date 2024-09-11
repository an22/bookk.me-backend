package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)