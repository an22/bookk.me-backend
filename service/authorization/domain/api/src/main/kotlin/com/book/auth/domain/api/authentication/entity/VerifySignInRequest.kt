package com.book.auth.domain.api.authentication.entity

import kotlinx.serialization.Serializable

@Serializable
class VerifySignInRequest(
    override val requestId: String,
    override val publicKeyCredentialJson: String,
    val deviceInfo: DeviceInfo
): FinishAssertionRequest {
    @Serializable
    data class DeviceInfo(
        val deviceUUID: String,
        val deviceName: String,
    )
}