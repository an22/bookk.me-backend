package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class SignInInfo(
    val login: String,
    val password: String,
    val totpCode: String,
    val deviceName: String
)