package com.bookk.notifications.domain.impl

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.notifications.domain.api.CreateDeviceEntry
import com.bookk.notifications.domain.datasource.DeviceDataSource
import kotlin.uuid.Uuid

internal class CreateDeviceEntryImpl(
    private val deviceDataSource: DeviceDataSource,
    private val transactionManager: TransactionManager,
) : CreateDeviceEntry {
    override suspend fun invoke(deviceUUID: Uuid, authId: Uuid, userId: Uuid) = transactionManager.transaction {
        deviceDataSource.create(authId = authId, deviceUUID = deviceUUID, userId = userId)
    }
}