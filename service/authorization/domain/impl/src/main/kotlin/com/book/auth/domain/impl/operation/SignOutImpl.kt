package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthLocalDataSource
import com.book.auth.domain.api.operation.SignOut

internal class SignOutImpl(
    private val localDataSource: UserAuthLocalDataSource
) : SignOut {
    override suspend fun call(params: SignOut.Param): Result<Unit> = runCatching {
        localDataSource.deleteTokenInfoForDevice(params.deviceId)
    }
}