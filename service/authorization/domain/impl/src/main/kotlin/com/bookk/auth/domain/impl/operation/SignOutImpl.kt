package com.bookk.auth.domain.impl.operation

import com.bookk.auth.domain.api.signout.operation.SignOut
import com.bookk.auth.domain.datasource.DeviceDataSource
import kotlin.uuid.Uuid

internal class SignOutImpl(
    private val deviceDataSource: DeviceDataSource
) : SignOut {

    override suspend fun invoke(deviceId: Uuid): Result<Unit> = runCatching {
        deviceDataSource.deleteTokenFromDevice(deviceId)
    }
}