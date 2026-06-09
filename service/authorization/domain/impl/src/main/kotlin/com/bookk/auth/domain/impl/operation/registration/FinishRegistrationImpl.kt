package com.bookk.auth.domain.impl.operation.registration

import com.bookk.auth.domain.api.authentication.entity.Authentication
import com.bookk.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.bookk.auth.domain.api.registration.entity.VerifyAccountCreationRequest.UserInfo
import com.bookk.auth.domain.api.registration.operation.FinishPasskeyRegistration
import com.bookk.auth.domain.api.registration.operation.FinishRegistration
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.bookk.auth.domain.datasource.AccountDataSource
import com.bookk.auth.domain.datasource.DeviceDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.auth.client.AuthEvent
import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.api.CreateUserRequest
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

    override suspend fun invoke(request: VerifyAccountCreationRequest): Result<AuthTokens> {
        return runCatching {
            val verifiedPasskey = finishPasskeyRegistration.verifyRequest(request).getOrThrow()
            var userId: Uuid? = null
            return transactionManager.transaction {
                userId = saveUserExternal(request)
                val ownerId = saveAuthorizationOwner(userId, verifiedPasskey.handle)
                finishPasskeyRegistration.attachOwner(ownerId, verifiedPasskey).getOrThrow()
                createAndSaveAuthCredentials(ownerId, request)
            }.onFailure {
                userId?.let { eventProducer.send(AuthEvent.UserDeleted(it)) }
            }
        }
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