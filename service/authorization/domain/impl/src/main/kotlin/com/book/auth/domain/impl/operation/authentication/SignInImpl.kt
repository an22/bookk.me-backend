package com.book.auth.domain.impl.operation.authentication

import com.book.auth.domain.api.authentication.entity.VerifySignInRequest
import com.book.auth.domain.api.authentication.entity.VerifySignInRequest.DeviceInfo
import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.api.authentication.operation.SignIn
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.book.auth.domain.datasource.DeviceDataSource

internal class SignInImpl(
    private val finishAssertion: FinishAssertion,
    private val generateAuthToken: GenerateAuthToken,
    private val deviceDataSource: DeviceDataSource
) : SignIn {
    override suspend fun invoke(request: VerifySignInRequest): Result<AuthTokens> = runCatching {
        val credentials = finishAssertion(request).getOrThrow()
        createDeviceIfNotExist(credentials.authInfo.id, request.deviceInfo)
        generateAuthToken(
            Source.FromAuthDevice(
                credentials.authInfo.id,
                request.deviceInfo.deviceUUID
            )
        ).getOrThrow()
    }

    private suspend fun createDeviceIfNotExist(ownerId: Long, deviceInfo: DeviceInfo) {
        deviceDataSource.createDeviceIfNotExist(
            authId = ownerId,
            uuid = deviceInfo.deviceUUID,
            name = deviceInfo.deviceName
        )
    }
}