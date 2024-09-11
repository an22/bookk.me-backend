package com.book.auth.domain.api.operation

import com.book.core.domain.operation.SuspendOperation

interface SignOut : SuspendOperation<SignOut.Param, Result<Unit>> {
    class Param(val deviceId: Long)
}