package com.book.auth.domain.api.datasource

import com.book.auth.domain.api.entity.DeviceAuthRecord
import com.book.auth.domain.api.entity.DeviceInfo
import com.book.auth.domain.api.entity.SignUpInfo
import com.book.auth.domain.api.entity.UserAuthRecord

interface UserAuthLocalDataSource {
    suspend fun saveUserRefreshToken(deviceId: Long, token: String)
    suspend fun createDevice(authRecordId: Long, deviceName: String): Long
    suspend fun createAuthRecord(userId: Long, passwordHash: String, totpSecret: String, info: SignUpInfo)
    suspend fun getDeviceAuthRecord(deviceName: String, login: String, passwordHash: String): DeviceAuthRecord?
    suspend fun getDeviceAuthRecord(deviceId: Long, refreshToken: String): DeviceAuthRecord?
    suspend fun getDevices(authRecordId: Long): List<DeviceInfo>
    suspend fun getDevice(authRecordId: Long, deviceName: String): DeviceInfo?
    suspend fun getAuthRecordByUsername(login: String): UserAuthRecord?
    suspend fun deleteTokenInfoForDevice(deviceId: Long)
}