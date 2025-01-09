package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.ChallengeResponse
import com.book.auth.domain.api.entity.CreateAccountRequest
import com.book.core.domain.entity.BusinessError

interface StartRegistration {

    suspend operator fun invoke(request: CreateAccountRequest): Result<ChallengeResponse>

    sealed class CreateUserAccountError(code: Int, message: String) : BusinessError(code, message) {
        data object EmailAlreadyExist : CreateUserAccountError(2, "This email already exists")
    }
}