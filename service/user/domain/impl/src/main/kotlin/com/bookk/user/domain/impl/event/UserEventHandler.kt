package com.bookk.user.domain.impl.event

import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.registerReceiver
import com.bookk.server.auth.client.AuthEvent
import com.bookk.user.domain.api.operation.DeleteUser
import kotlinx.coroutines.CoroutineScope

internal class UserEventHandler(
    private val consumer: StandardEventConsumer,
    private val deleteUser: DeleteUser
) : EventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerReceiver(AuthEvent.UserDeleted.TOPIC) { event: AuthEvent.UserDeleted ->
                deleteUser(event.userId)
            }
            .start(scope)
    }
}