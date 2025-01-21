package com.book.auth.domain.api.authentication.entity

import kotlinx.serialization.Serializable

@Serializable
class VerifySignInRequest(
    val requestId: String,
    val deviceInfo: DeviceInfo,
    val publicKeyCredentialJson: String
) {
    @Serializable
    data class DeviceInfo(
        val deviceUUID: String,
        val deviceName: String,
    )
}