package com.book.auth.domain.api.registration.entity

import kotlinx.serialization.Serializable

@Serializable
data class VerifyAccountCreationRequest(
    val deviceInfo: DeviceInfo,
    val userInfo: UserInfo,
    val publicKeyCredentialJson: String
) {
    @Serializable
    data class UserInfo(
        val userId: String,
        val name: String,
        val lastName: String,
        val email: String
    )

    @Serializable
    data class DeviceInfo(
        val deviceUUID: String,
        val deviceName: String,
    )
}