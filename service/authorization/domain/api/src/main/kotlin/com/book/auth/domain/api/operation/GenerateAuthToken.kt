package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.SignInInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.core.domain.entity.BusinessError
import com.book.core.domain.operation.SuspendOperation

interface GenerateAuthToken : SuspendOperation<GenerateAuthToken.Param, Result<TokenInfo>> {

    sealed interface Param {
        class FromCredentials(val info: SignInInfo) : Param
        class FromRefresh(val userId: Long, val refreshToken: String) : Param
    }

    sealed class GenerateAuthTokenBusinessError(code: Int) : BusinessError(code) {
        data object InvalidCredentials : GenerateAuthTokenBusinessError(1)
    }
}