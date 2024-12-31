package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class PassKeySignUpStartInfo(
    val firstName: String,
    val lastName: String,
    val email: String
)