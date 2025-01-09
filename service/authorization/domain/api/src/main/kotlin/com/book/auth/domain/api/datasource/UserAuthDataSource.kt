package com.book.auth.domain.api.datasource

import com.book.auth.domain.api.entity.Authentication
import com.book.auth.domain.api.entity.Device

interface UserAuthDataSource {
    //Account
    suspend fun createAuthorization(info: Authentication): Authentication
    suspend fun getAuthRecordById(id: Long): Authentication?
    suspend fun getAuthRecordByEmail(email: String): Authentication?
    suspend fun getAuthRecordByUserId(userId: Long): Authentication?
    suspend fun deleteAuthorization(authId: Long)

    //Token
    suspend fun saveUserRefreshToken(deviceId: Long, token: String)
    suspend fun deleteTokenInfoForDevice(deviceId: Long)

    //Device
    suspend fun createDevice(authId: Long, uuid: String, name: String): Device
    suspend fun getDeviceById(deviceId: Long): Device?
    suspend fun getDeviceByUUID(deviceUUID: String): Device?
    suspend fun getDevices(authId: Long): List<Device>
}