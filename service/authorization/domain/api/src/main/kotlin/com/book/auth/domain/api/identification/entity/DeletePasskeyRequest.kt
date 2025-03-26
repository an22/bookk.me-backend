package com.book.auth.domain.api.identification.entity

import kotlinx.serialization.Serializable

@Serializable
class DeletePasskeyRequest(
    val id: Long
)