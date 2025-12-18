package com.bookk.auth.domain.api.registration.entity

import com.bookk.auth.domain.api.authentication.entity.FinishRegistrationRequest
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class VerifyAccountCreationRequest(
    override val requestId: String,
    val deviceInfo: DeviceInfo,
    val userInfo: UserInfo,
    override val publicKeyCredentialJson: String
) : FinishRegistrationRequest {
    @Serializable
    data class UserInfo(
        val name: String,
        val lastName: String,
        val email: String
    )

    @Serializable
    data class DeviceInfo(
        val deviceUUID: Uuid,
        val deviceName: String,
    )
}