package com.bookk.user.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class UserId(
    val id: Uuid
)