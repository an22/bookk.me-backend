package com.bookk.server.user.client.api

import com.book.user.domain.api.entity.User
import kotlinx.serialization.Serializable

@Serializable
class UserSnapshot(
    val id: Long,
    val name: String,
    val lastName: String,
    val email: String
) {
    companion object {
        internal fun fromUser(user: User): UserSnapshot {
            return UserSnapshot(
                id = user.id,
                name = user.name,
                lastName = user.lastName,
                email = user.email
            )
        }
    }
}