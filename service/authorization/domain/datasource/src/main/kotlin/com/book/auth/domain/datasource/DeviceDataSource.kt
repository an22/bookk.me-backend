package com.book.auth.domain.datasource

import com.book.auth.domain.api.identification.entity.Device

interface DeviceDataSource {
    suspend fun createDeviceIfNotExist(authId: Long, uuid: String, name: String)
    suspend fun getDeviceById(deviceId: Long): Device?
    suspend fun getDeviceByAuthIdAndUUID(authId: Long, deviceUUID: String): Device?
    suspend fun getDevices(authId: Long): List<Device>
    suspend fun attachRefreshTokenToDevice(deviceId: Long, tokenId: String)
    suspend fun deleteTokenFromDevice(deviceId: Long)
}