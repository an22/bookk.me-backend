package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.SignUpInfo
import com.book.auth.domain.api.entity.TotpSecret
import com.book.core.domain.entity.BusinessError
import com.book.core.domain.operation.SuspendOperation

interface CreateUserAccount : SuspendOperation<SignUpInfo, Result<TotpSecret>> {
    sealed class CreateUserAccountError(code: Int, message: String) : BusinessError(code, message) {
        data object EmailOrPhoneAlreadyExist : CreateUserAccountError(1, "This email or phone already exists")
        data object LoginAlreadyExist : CreateUserAccountError(2, "This username already exists")
        data object PasswordTooShort : CreateUserAccountError(3, "Password should have at least %s characters")
        data object PasswordTooWeak : CreateUserAccountError(4, "Password should have at least 1 uppercase character, 1 number and 1 special character.")
    }
}