package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.DeleteAccountInfo
import com.book.core.domain.entity.BusinessError
import com.book.core.domain.operation.SuspendOperation

interface DeleteAccount : SuspendOperation<DeleteAccount.Param, Result<Unit>> {
    class Param(
        val userName: String,
        val info: DeleteAccountInfo
    )

    sealed class DeleteAccountError(code: Int, message: String) : BusinessError(code, message) {
        data object InvalidCredentials : DeleteAccountError(1, "Invalid credentials")
    }
}