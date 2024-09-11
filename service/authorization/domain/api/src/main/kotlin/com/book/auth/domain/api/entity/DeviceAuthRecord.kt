package com.book.auth.domain.api.entity

class DeviceAuthRecord(
    val authRecord: UserAuthRecord,
    val deviceInfo: DeviceInfo
)