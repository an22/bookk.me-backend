package com.book.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class UserExistInfo(
    val exists: Boolean
)