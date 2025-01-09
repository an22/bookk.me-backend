package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.DeleteAccountInfo
import com.book.core.domain.entity.BusinessError

interface DeleteAccount {

    suspend operator fun invoke(userId: Long, info: DeleteAccountInfo): Result<Unit>

    sealed class DeleteAccountError(code: Int, message: String) : BusinessError(code, message) {
        data object InvalidCredentials : DeleteAccountError(1, "Invalid credentials")
        data object UnableToDeleteAccount : DeleteAccountError(2, "Error while deleting account. Try again later.")
    }
}