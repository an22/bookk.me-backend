package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class RefreshTokenInfo(
    val userId: Long,
    val refreshToken: String
)