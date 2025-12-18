package com.bookk.auth.domain.api.authentication.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class VerifySignInRequest(
    override val requestId: String,
    override val publicKeyCredentialJson: String,
    val deviceInfo: DeviceInfo
): FinishAssertionRequest {
    @Serializable
    data class DeviceInfo(
        val deviceUUID: Uuid,
        val deviceName: String
    )
}