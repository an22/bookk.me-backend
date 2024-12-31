package com.book.user.domain.api.operation

import com.book.core.domain.entity.BusinessError
import com.book.core.domain.operation.SuspendOperation
import com.book.user.domain.api.entity.User

interface GetUserById : SuspendOperation<Long, Result<User>> {
    sealed class GetCurrentUserError(code: Int) : BusinessError(code) {
        data object UserNotFound : GetCurrentUserError(1)
    }
}