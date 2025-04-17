package com.book.business.data.map

import com.book.user.data.orm.entity.UserEntity
import com.book.user.domain.api.entity.User

fun UserEntity.toDomain(): User {
    return User(
        id = id.value,
        name = name,
        lastName = lastName,
        email = email
    )
}