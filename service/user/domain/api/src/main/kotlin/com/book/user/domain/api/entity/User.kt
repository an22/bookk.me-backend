package com.book.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class User(
    val id: Long,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String?
)