package com.bookk.user.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserEditModel(
    val id: Uuid? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)