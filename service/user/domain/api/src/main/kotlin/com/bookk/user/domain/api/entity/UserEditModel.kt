package com.bookk.user.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserEditModel(
    val id: Uuid?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?
)
