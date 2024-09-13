package com.book.user.domain.api.operation

import com.book.core.domain.operation.SuspendOperation

interface DeleteUser : SuspendOperation<DeleteUser.Param, Result<Unit>> {
    class Param(val userId: Long)
}