package com.bookk.business.domain.impl.event

import com.bookk.business.domain.api.business.operation.DeleteBusiness
import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.registerResultReceiver
import com.bookk.server.auth.client.AuthEvent
import kotlinx.coroutines.CoroutineScope

internal class BusinessEventHandlerImpl(
    private val consumer: StandardEventConsumer,
    private val deleteBusiness: DeleteBusiness
) : EventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerResultReceiver(AuthEvent.UserDeleted.TOPIC) { event: AuthEvent.UserDeleted ->
                deleteBusiness(event.userId)
            }
            .start(scope)
    }
}