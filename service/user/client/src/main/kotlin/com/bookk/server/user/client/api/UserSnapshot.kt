package com.bookk.server.user.client.api

import com.bookk.user.domain.api.entity.User
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class UserSnapshot(
    val id: Uuid,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String?
) {
    companion object {
        internal fun fromUser(user: User): UserSnapshot {
            return UserSnapshot(
                id = user.id,
                name = user.name,
                lastName = user.lastName,
                email = user.email,
                phone = user.phone
            )
        }

        fun stub(
            id: Uuid = Uuid.random(),
            name: String = "Alice",
            lastName: String = "Smith",
            email: String = "user@example.com",
            phone: String? = null
        ) = UserSnapshot(id = id, name = name, lastName = lastName, email = email, phone = phone)
    }
}