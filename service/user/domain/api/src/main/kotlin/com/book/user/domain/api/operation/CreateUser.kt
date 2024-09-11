package com.book.user.domain.api.operation

import com.book.core.domain.entity.BusinessError
import com.book.core.domain.operation.SuspendOperation
import com.book.user.domain.api.entity.User

interface CreateUser : SuspendOperation<User, Result<Long>> {
    sealed class CreateUserError(code: Int) : BusinessError(code)
}