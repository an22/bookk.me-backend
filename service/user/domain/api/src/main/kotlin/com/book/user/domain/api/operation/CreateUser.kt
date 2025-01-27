package com.book.user.domain.api.operation

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.entity.UserId

interface CreateUser {

    suspend operator fun invoke(user: User): Result<UserId>

}