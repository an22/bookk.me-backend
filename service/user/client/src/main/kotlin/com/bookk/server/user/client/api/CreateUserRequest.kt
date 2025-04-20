package com.bookk.server.user.client.api

import kotlinx.serialization.Serializable

@Serializable
class CreateUserRequest(
    val name: String,
    val lastName: String,
    val email: String
)