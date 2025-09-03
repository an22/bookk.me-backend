package com.book.auth.domain.api.token.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class RefreshTokenInfo(
    val tokenId: Uuid,
    val deviceId: Uuid
)