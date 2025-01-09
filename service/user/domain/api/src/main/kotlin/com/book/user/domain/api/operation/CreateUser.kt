package com.book.user.domain.api.operation

import com.book.core.domain.entity.BusinessError
import com.book.user.domain.api.entity.User

interface CreateUser {

    suspend operator fun invoke(user: User): Result<Long>

    sealed class CreateUserError(code: Int) : BusinessError(code)
}