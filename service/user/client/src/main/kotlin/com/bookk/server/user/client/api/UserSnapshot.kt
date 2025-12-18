package com.bookk.server.user.client.api

import com.bookk.user.domain.api.entity.User
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class UserSnapshot(
    val id: Uuid,
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