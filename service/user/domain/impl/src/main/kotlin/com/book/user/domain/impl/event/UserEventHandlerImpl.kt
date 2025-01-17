package com.book.user.domain.impl.event

import com.book.core.data.eventstreaming.StandardEventConsumer
import com.book.core.data.eventstreaming.registerReceiver
import com.book.user.domain.api.event.UserEventHandler
import com.book.user.domain.api.event.UserEvents.DeleteUserEvent
import com.book.user.domain.api.operation.DeleteUser
import kotlinx.coroutines.CoroutineScope

class UserEventHandlerImpl(
    private val consumer: StandardEventConsumer,
    private val deleteUser: DeleteUser,
) : UserEventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerReceiver(DeleteUserEvent.TOPIC) { event: DeleteUserEvent ->
                deleteUser(event.userId)
            }
            .start(scope)
    }
}