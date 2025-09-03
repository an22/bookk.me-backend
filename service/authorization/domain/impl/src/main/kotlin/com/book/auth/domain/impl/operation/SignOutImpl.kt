package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.signout.operation.SignOut
import com.book.auth.domain.datasource.DeviceDataSource
import kotlin.uuid.Uuid

internal class SignOutImpl(
    private val deviceDataSource: DeviceDataSource
) : SignOut {

    override suspend fun invoke(deviceId: Uuid): Result<Unit> = runCatching {
        deviceDataSource.deleteTokenFromDevice(deviceId)
    }
}