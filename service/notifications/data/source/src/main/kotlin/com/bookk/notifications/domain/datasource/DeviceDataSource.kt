package com.bookk.notifications.domain.datasource

import com.bookk.notifications.domain.api.entity.Device
import kotlin.uuid.Uuid

interface DeviceDataSource {
    suspend fun create(authId: Uuid, deviceUUID: Uuid, userId: Uuid): Device
    suspend fun getById(id: Uuid): Device?
    suspend fun getByDeviceUuid(deviceUuid: Uuid): Device?
    suspend fun getByAuthId(authId: Uuid): Device?
    suspend fun getByUserId(userId: Uuid): List<Device>
    suspend fun updateToken(deviceUuid: Uuid, token: String): Device
    suspend fun deleteByDeviceUuid(deviceUuid: Uuid)
}
