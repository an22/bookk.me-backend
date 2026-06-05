package com.bookk.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
data class EmailBody(
    val email: String
)