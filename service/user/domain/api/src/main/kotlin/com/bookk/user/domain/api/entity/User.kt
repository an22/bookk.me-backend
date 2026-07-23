package com.bookk.user.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class User(
    val id: Uuid,
    val name: String,
    val lastName: String,
    val email: String
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            name: String = "Alice",
            lastName: String = "Smith",
            email: String = "user@example.com"
        ) = User(id, name, lastName, email)
    }
}