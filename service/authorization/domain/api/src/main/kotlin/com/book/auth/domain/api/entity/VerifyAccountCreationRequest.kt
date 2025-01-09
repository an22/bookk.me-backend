package com.book.auth.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class VerifyAccountCreationRequest(
    val deviceInfo: DeviceInfo,
    val userInfo: UserInfo,
    val publicKeyCredentialJson: String
) {
    @Serializable
    class UserInfo(
        val name: String,
        val lastName: String,
        val email: String
    )

    @Serializable
    class DeviceInfo(
        val deviceUUID: String,
        val deviceName: String,
    )
}