package com.book.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class UserEditModel(
    val id: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)