package com.book.auth.domain.api.entity

class DeviceInfo(
    val id: Long,
    val refreshToken: String,
    val deviceName: String,
    val isSignedIn: Boolean
)