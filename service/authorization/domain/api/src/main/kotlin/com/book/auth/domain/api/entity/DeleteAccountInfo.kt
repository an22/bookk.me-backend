package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class DeleteAccountInfo(
    val password: String,
    val totpCode: String
)