package com.bookk.user.domain.impl.event

import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.registerReceiver
import com.bookk.server.user.client.api.event.UserEvents.DeleteUserEvent
import com.bookk.user.domain.api.operation.DeleteUser
import kotlinx.coroutines.CoroutineScope

class UserEventHandlerImpl(
    private val consumer: StandardEventConsumer,
    private val deleteUser: DeleteUser
) : EventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerReceiver(DeleteUserEvent.TOPIC) { event: DeleteUserEvent ->
                deleteUser(event.userId)
            }
            .start(scope)
    }
}