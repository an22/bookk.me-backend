package com.book.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class User(
    val id: Long,
    val name: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val role: UserRole
)