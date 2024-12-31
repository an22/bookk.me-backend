package com.book.auth.domain.api.entity

import com.book.user.domain.api.entity.UserRole

class UserAuthRecord(
    val id: Long,
    val userId: Long,
    val role: UserRole,
    val login: String,
    val totpSecret: String,
    val passwordHash: String
)

class UserAuthRecordV2(
    val id: Long,
    val login: String,
    val role: UserRole,
)