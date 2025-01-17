package com.book.auth.domain.datasource

import com.book.auth.domain.api.entity.Device

interface DeviceDataSource {
    suspend fun createDevice(authId: Long, uuid: String, name: String): Device
    suspend fun getDeviceById(deviceId: Long): Device?
    suspend fun getDeviceByUUID(deviceUUID: String): Device?
    suspend fun getDevices(authId: Long): List<Device>
    suspend fun attachRefreshTokenToDevice(deviceId: Long, token: String)
    suspend fun deleteTokenFromDevice(deviceId: Long)
}