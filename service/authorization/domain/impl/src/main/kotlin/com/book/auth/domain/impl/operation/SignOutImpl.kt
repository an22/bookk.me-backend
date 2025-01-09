package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.operation.SignOut

internal class SignOutImpl(
    private val localDataSource: UserAuthDataSource
) : SignOut {

    override suspend fun invoke(deviceId: Long): Result<Unit> = runCatching {
        localDataSource.deleteTokenInfoForDevice(deviceId)
    }
}