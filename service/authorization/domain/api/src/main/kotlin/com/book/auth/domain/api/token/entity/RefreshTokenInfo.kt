package com.book.auth.domain.api.token.entity

import kotlinx.serialization.Serializable

@Serializable
class RefreshTokenInfo(
    val userId: Long,
    val refreshToken: String
)