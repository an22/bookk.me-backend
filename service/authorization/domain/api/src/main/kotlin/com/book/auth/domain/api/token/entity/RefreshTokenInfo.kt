package com.book.auth.domain.api.token.entity

import kotlinx.serialization.Serializable

@Serializable
class RefreshTokenInfo(
    val tokenId: String,
    val deviceId: Long
)