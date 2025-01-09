package com.book.auth.data.map

import com.book.auth.data.orm.entity.AuthDeviceEntity
import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.domain.api.entity.Authentication
import com.book.auth.domain.api.entity.Device
import com.book.auth.domain.api.entity.DeviceInfo

internal fun AuthenticationEntity.toDomain(): Authentication {
    return Authentication(
        id = id.value,
        userId = userId,
        email = email
    )
}

internal fun AuthDeviceEntity.toDomain(): Device {
    return Device(
        authRecord = userAuth.toDomain(),
        deviceInfo = DeviceInfo(
            id = id.value,
            deviceUUID = deviceUUID,
            refreshToken = refreshToken,
            deviceName = deviceName,
            isSignedIn = isSignedIn
        )
    )
}