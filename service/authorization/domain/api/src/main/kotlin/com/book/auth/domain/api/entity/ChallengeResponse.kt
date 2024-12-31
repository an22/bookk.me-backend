package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class ChallengeResponse(
    val challenge: String
)