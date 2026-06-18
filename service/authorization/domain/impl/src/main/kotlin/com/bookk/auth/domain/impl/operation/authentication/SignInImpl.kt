package com.bookk.auth.domain.impl.operation.authentication

import com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest
import com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest.DeviceInfo
import com.bookk.auth.domain.api.authentication.operation.FinishAssertion
import com.bookk.auth.domain.api.authentication.operation.SignIn
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.bookk.auth.domain.datasource.DeviceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.uuid.Uuid

internal class SignInImpl(
    private val finishAssertion: FinishAssertion,
    private val generateAuthToken: GenerateAuthToken,
    private val deviceDataSource: DeviceDataSource,
    private val transactionManager: TransactionManager
) : SignIn {

    override suspend fun invoke(request: VerifySignInRequest): Result<AuthTokens> = transactionManager.transaction {
        val credentials = finishAssertion(request).getOrThrow()
        createDeviceIfNotExist(credentials.authInfo.id, request.deviceInfo)
        generateAuthToken(
            Source.InitialAuthentication(
                credentials.authInfo.id,
                request.deviceInfo.deviceUUID
            )
        ).getOrThrow()
    }

    private suspend fun createDeviceIfNotExist(ownerId: Uuid, deviceInfo: DeviceInfo) {
        deviceDataSource.createDeviceIfNotExist(
            authId = ownerId,
            uuid = deviceInfo.deviceUUID,
            name = deviceInfo.deviceName
        )
    }
}