package com.book.auth.domain.api.entity

import com.book.user.domain.api.entity.UserRole
import kotlinx.serialization.Serializable

@Serializable
class PasskeySignUpInfo(
    val name: String,
    val lastName: String,
    val email: String,
    val role: UserRole,
    val publicKeyCredentialJson: String,
    val businessName: String?
)