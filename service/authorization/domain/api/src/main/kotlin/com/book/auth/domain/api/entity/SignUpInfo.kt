package com.book.auth.domain.api.entity

import com.book.user.domain.api.entity.UserRole
import kotlinx.serialization.Serializable

@Serializable
class SignUpInfo(
    val login: String,
    val password: String,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: UserRole
)