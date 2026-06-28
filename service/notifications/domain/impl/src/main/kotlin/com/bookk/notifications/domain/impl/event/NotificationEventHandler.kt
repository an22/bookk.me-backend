package com.bookk.notifications.domain.impl.event

import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.registerResultReceiver
import com.bookk.notifications.domain.api.CreateDeviceEntry
import com.bookk.notifications.domain.api.DeleteDeviceByUUID
import com.bookk.server.auth.client.AuthEvent
import kotlinx.coroutines.CoroutineScope

internal class NotificationEventHandler(
    private val consumer: StandardEventConsumer,
    private val createDeviceEntry: CreateDeviceEntry,
    private val deleteDeviceByUUID: DeleteDeviceByUUID,
) : EventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerResultReceiver(AuthEvent.DeviceCreated.TOPIC) { event: AuthEvent.DeviceCreated ->
                createDeviceEntry(event.deviceUuid, event.authId, event.userId)
            }
            .registerResultReceiver(AuthEvent.DeviceDeleted.TOPIC) { event: AuthEvent.DeviceDeleted ->
                deleteDeviceByUUID(event.deviceUuid)
            }
            .start(scope)
    }
}