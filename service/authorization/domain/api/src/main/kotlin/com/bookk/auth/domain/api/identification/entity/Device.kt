package com.bookk.auth.domain.api.identification.entity

import com.bookk.auth.domain.api.authentication.entity.Authentication
import kotlin.uuid.Uuid

class Device(
    val authRecord: Authentication,
    val deviceInfo: DeviceInfo
)

class DeviceInfo(
    val id: Uuid,
    val deviceUUID: Uuid,
    val refreshTokenId: Uuid?,
    val deviceName: String,
    val isSignedIn: Boolean
)