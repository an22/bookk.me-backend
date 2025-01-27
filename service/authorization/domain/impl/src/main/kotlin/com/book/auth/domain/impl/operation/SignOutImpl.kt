package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.signout.operation.SignOut
import com.book.auth.domain.datasource.DeviceDataSource

internal class SignOutImpl(
    private val deviceDataSource: DeviceDataSource
) : SignOut {

    override suspend fun invoke(deviceId: Long): Result<Unit> = runCatching {
        deviceDataSource.deleteTokenFromDevice(deviceId)
    }
}