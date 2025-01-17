package com.book.auth.domain.api.entity

class Device(
    val authRecord: Authentication,
    val deviceInfo: DeviceInfo
)

class DeviceInfo(
    val id: Long,
    val deviceUUID: String,
    val refreshToken: String?,
    val deviceName: String,
    val isSignedIn: Boolean
)