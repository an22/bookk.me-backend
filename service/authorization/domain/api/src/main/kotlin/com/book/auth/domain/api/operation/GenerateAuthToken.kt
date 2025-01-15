package com.book.auth.domain.api.operation

import com.book.auth.domain.api.entity.SignInInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.core.domain.entity.BusinessError

interface GenerateAuthToken {

    suspend operator fun invoke(source: Source): Result<TokenInfo>

    sealed interface Source {
        class FromPublicKey(val info: SignInInfo) : Source
        class FromRefresh(val userId: Long, val refreshToken: String) : Source
        class FromDeviceUUID(val deviceUUID: String) : Source
    }

    sealed class GenerateAuthTokenBusinessError(code: Int) : BusinessError(code) {
        data object InvalidCredentials : GenerateAuthTokenBusinessError(1)
    }
}