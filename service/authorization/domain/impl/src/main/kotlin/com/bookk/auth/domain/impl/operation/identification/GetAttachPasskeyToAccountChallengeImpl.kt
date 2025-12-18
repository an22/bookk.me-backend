package com.bookk.auth.domain.impl.operation.identification

import com.bookk.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge
import com.bookk.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge.Error.UnableToGeneratePasskeyChallenge
import com.bookk.auth.domain.api.registration.entity.RegistrationChallengeResponse
import com.bookk.auth.domain.api.registration.operation.StartPasskeyRegistration
import com.bookk.auth.domain.datasource.AccountDataSource
import com.bookk.auth.domain.datasource.DeviceDataSource
import com.bookk.server.user.client.UserClient
import kotlin.uuid.Uuid

internal class GetAttachPasskeyToAccountChallengeImpl(
    private val startPasskeyRegistration: StartPasskeyRegistration,
    private val accountDataSource: AccountDataSource,
    private val deviceDataSource: DeviceDataSource,
    private val userClient: UserClient
) : GetAttachPasskeyToAccountChallenge {
    override suspend fun invoke(authId: Uuid, deviceId: Uuid, userId: Uuid): Result<RegistrationChallengeResponse> = runCatching {
        val auth = accountDataSource.getAuthRecordById(authId) ?: throw UnableToGeneratePasskeyChallenge
        val device = deviceDataSource.getDeviceById(deviceId) ?: throw UnableToGeneratePasskeyChallenge
        val user = userClient.getUserById(userId).getOrElse { throw UnableToGeneratePasskeyChallenge }
        val handle = auth.uuid
        val displayName = "${user.name} ${user.lastName} - ${device.deviceInfo.deviceName}"
        return startPasskeyRegistration(handle, displayName)
    }
}