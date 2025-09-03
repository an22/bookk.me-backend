package com.book.user.data.map

import com.book.user.data.orm.entity.UserEntity
import com.book.user.domain.api.entity.User
import kotlin.uuid.toKotlinUuid

fun UserEntity.toDomain(): User {
    return User(
        id = id.value.toKotlinUuid(),
        name = name,
        lastName = lastName,
        email = email
    )
}