package com.book.user.data.map

import com.book.user.data.orm.UserColumn
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.entity.UserRole
import org.ktorm.dsl.QueryRowSet

fun QueryRowSet.toUser(): User {
    return User(
        id = get(UserColumn.id)!!,
        name = get(UserColumn.name)!!,
        lastName = get(UserColumn.lastName)!!,
        phone = get(UserColumn.phone)!!,
        email = get(UserColumn.email)!!,
        role = UserRole.entries.first { it.id == get(UserColumn.role)!! }
    )
}