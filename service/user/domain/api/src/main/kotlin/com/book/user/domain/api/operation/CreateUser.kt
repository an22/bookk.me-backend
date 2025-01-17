package com.book.user.domain.api.operation

import com.book.user.domain.api.entity.User

interface CreateUser {

    suspend operator fun invoke(user: User): Result<Long>

}