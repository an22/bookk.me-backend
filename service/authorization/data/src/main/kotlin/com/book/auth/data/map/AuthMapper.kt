package com.book.auth.data.map

import com.book.auth.data.orm.AuthDevice
import com.book.auth.data.orm.UserAuthInfo
import com.book.auth.domain.api.entity.DeviceAuthRecord
import com.book.auth.domain.api.entity.DeviceInfo
import com.book.auth.domain.api.entity.UserAuthRecord
import com.book.user.domain.api.entity.UserRole
import org.ktorm.dsl.QueryRowSet

fun QueryRowSet.toUserAuthRecord(): UserAuthRecord {
    return UserAuthRecord(
        id = get(UserAuthInfo.id)!!,
        userId = get(UserAuthInfo.userId)!!,
        login = get(UserAuthInfo.login)!!,
        passwordHash = get(UserAuthInfo.passwordHash)!!,
        role = UserRole.entries.first { it.id == get(UserAuthInfo.role) },
        totpSecret = get(UserAuthInfo.totpSecret)!!
    )
}

fun QueryRowSet.toDeviceInfo():DeviceInfo {
    return DeviceInfo(
        id = get(AuthDevice.id)!!,
        refreshToken = get(AuthDevice.refreshToken)!!,
        isSignedIn = get(AuthDevice.isSignedIn)!!,
        deviceName = get(AuthDevice.deviceName)!!
    )
}

fun QueryRowSet.toDeviceAuthRecord(): DeviceAuthRecord {
    return DeviceAuthRecord(
        authRecord = toUserAuthRecord(),
        deviceInfo = toDeviceInfo()
    )
}