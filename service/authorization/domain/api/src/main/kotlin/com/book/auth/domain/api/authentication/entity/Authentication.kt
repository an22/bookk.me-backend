package com.book.auth.domain.api.authentication.entity

import kotlin.uuid.Uuid

data class Authentication(
    val id: Uuid,
    val userId: Uuid,
    val uuid: Uuid
)