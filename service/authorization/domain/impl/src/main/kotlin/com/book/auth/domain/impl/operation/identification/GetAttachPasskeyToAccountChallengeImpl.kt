package com.book.auth.domain.impl.operation.identification

import com.book.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge
import com.book.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge.Error.UnableToGeneratePasskeyChallenge
import com.book.auth.domain.api.registration.entity.RegistrationChallengeResponse
import com.book.auth.domain.api.registration.operation.StartPasskeyRegistration
import com.book.auth.domain.datasource.AccountDataSource
import com.book.auth.domain.datasource.DeviceDataSource
import com.bookk.core.toUUIDBytes
import com.bookk.server.user.client.UserClient

internal class GetAttachPasskeyToAccountChallengeImpl(
    private val startPasskeyRegistration: StartPasskeyRegistration,
    private val accountDataSource: AccountDataSource,
    private val deviceDataSource: DeviceDataSource,
    private val userClient: UserClient
) : GetAttachPasskeyToAccountChallenge {
    override suspend fun invoke(authId: Long, deviceId: Long, userId: Long): Result<RegistrationChallengeResponse> = runCatching {
        val auth = accountDataSource.getAuthRecordById(authId) ?: throw UnableToGeneratePasskeyChallenge
        val device = deviceDataSource.getDeviceById(deviceId) ?: throw UnableToGeneratePasskeyChallenge
        val user = userClient.getUserById(userId).getOrElse { throw UnableToGeneratePasskeyChallenge }
        val handle = auth.uuid.toUUIDBytes()
        val displayName = "${user.name} ${user.lastName} - ${device.deviceInfo.deviceName}"
        return startPasskeyRegistration(handle, displayName)
    }
}