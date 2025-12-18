package com.bookk.user.data.map

import com.bookk.user.data.orm.entity.UserEntity
import com.bookk.user.domain.api.entity.User
import kotlin.uuid.toKotlinUuid

fun UserEntity.toDomain(): User {
    return User(
        id = id.value.toKotlinUuid(),
        name = name,
        lastName = lastName,
        email = email
    )
}