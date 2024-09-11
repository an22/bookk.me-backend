package com.book.user.domain.api.operation

import com.book.core.domain.operation.SuspendOperation
import com.book.user.domain.api.operation.IsUserExistWithParameters.Param

interface IsUserExistWithParameters : SuspendOperation<Param, Result<Boolean>> {
    class Param(
        val phone: String,
        val email: String
    )
}