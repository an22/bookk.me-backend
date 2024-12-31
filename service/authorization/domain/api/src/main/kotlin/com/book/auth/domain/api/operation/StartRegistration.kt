package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.ChallengeResponse
import com.book.auth.domain.api.entity.PassKeySignUpStartInfo
import com.book.core.domain.entity.BusinessError
import com.book.core.domain.operation.SuspendOperation

interface StartRegistration : SuspendOperation<PassKeySignUpStartInfo, Result<ChallengeResponse>> {
    sealed class CreateUserAccountError(code: Int, message: String) : BusinessError(code, message) {
        data object EmailAlreadyExist : CreateUserAccountError(2, "This email already exists")
    }
}