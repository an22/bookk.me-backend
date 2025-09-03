package com.book.auth.domain.datasource

import com.book.auth.domain.api.identification.entity.Device
import kotlin.uuid.Uuid

interface DeviceDataSource {
    suspend fun createDeviceIfNotExist(authId: Uuid, uuid: Uuid, name: String)
    suspend fun getDeviceById(deviceId: Uuid): Device?
    suspend fun getDeviceByAuthIdAndUUID(authId: Uuid, deviceUUID: Uuid): Device?
    suspend fun getDevices(authId: Uuid): List<Device>
    suspend fun attachRefreshTokenToDevice(deviceId: Uuid, tokenId: Uuid)
    suspend fun deleteTokenFromDevice(deviceId: Uuid)
}