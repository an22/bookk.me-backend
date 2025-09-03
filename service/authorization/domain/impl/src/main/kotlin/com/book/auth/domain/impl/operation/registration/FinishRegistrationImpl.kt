package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.authentication.entity.Authentication
import com.book.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.registration.entity.VerifyAccountCreationRequest.UserInfo
import com.book.auth.domain.api.registration.operation.FinishPasskeyRegistration
import com.book.auth.domain.api.registration.operation.FinishRegistration
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.book.auth.domain.datasource.AccountDataSource
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.core.data.eventstreaming.StandardEventProducer
import com.book.core.data.eventstreaming.send
import com.book.core.domain.transaction.TransactionManager
import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.api.CreateUserRequest
import com.bookk.server.user.client.api.event.UserEvents.DeleteUserEvent
import kotlin.uuid.Uuid

internal class FinishRegistrationImpl(
    private val accountDataSource: AccountDataSource,
    private val deviceDataSource: DeviceDataSource,
    private val userClient: UserClient,
    private val generateAuthToken: GenerateAuthToken,
    private val eventProducer: StandardEventProducer,
    private val transactionManager: TransactionManager,
    private val finishPasskeyRegistration: FinishPasskeyRegistration
) : FinishRegistration {

    override suspend fun invoke(request: VerifyAccountCreationRequest) = runCatching {
        val verifiedPasskey = finishPasskeyRegistration.verifyRequest(request).getOrThrow()
        val userId = saveUserExternal(request)
        transactionManager.runInTransaction {
            val ownerId = saveAuthorizationOwner(userId, verifiedPasskey.handle)
            finishPasskeyRegistration.attachOwner(ownerId, verifiedPasskey)
            createAndSaveAuthCredentials(ownerId, request)
        }.onFailure {
            eventProducer.send(DeleteUserEvent(userId))
        }.getOrThrow()
    }

    private suspend fun createAndSaveAuthCredentials(ownerId: Uuid, request: VerifyAccountCreationRequest): AuthTokens {
        deviceDataSource.createDeviceIfNotExist(
            authId = ownerId,
            uuid = request.deviceInfo.deviceUUID,
            name = request.deviceInfo.deviceName
        )
        return generateAuthToken(Source.FromAuthDevice(ownerId, request.deviceInfo.deviceUUID)).getOrThrow()
    }

    private suspend fun saveUserExternal(request: VerifyAccountCreationRequest): Uuid {
        return userClient.createUser(createUserFrom(request.userInfo)).getOrThrow()
    }

    private suspend fun saveAuthorizationOwner(userId: Uuid, handle: Uuid): Uuid {
        val authentication = Authentication(
            id = Uuid.random(),
            userId = userId,
            uuid = handle
        )
        return accountDataSource.createAuthorization(authentication).id
    }

    private fun createUserFrom(info: UserInfo): CreateUserRequest {
        return CreateUserRequest(
            name = info.name,
            lastName = info.lastName,
            email = info.email
        )
    }
}