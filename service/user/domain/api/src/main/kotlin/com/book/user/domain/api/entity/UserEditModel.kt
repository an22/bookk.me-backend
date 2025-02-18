package com.book.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class UserEditModel(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)

@Serializable
class ShortUserEditModel(
    val firstName: String? = null,
    val lastName: String? = null,
) {
    fun expand() = UserEditModel(
        firstName = firstName,
        lastName = lastName
    )
}