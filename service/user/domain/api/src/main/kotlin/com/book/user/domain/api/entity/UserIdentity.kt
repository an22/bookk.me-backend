package com.book.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class UserIdentity(
    val phone: String,
    val email: String
)