package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.operation.SignOut

internal class SignOutImpl(
    private val localDataSource: UserAuthDataSource
) : SignOut {
    override suspend fun call(params: SignOut.Param): Result<Unit> = runCatching {
        localDataSource.deleteTokenInfoForDevice(params.deviceId)
    }
}