package com.bookk.business.domain.impl.event

import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.registerReceiver
import com.bookk.server.business.client.api.event.BusinessEvent.DeleteBusinessesForUserEvent
import kotlinx.coroutines.CoroutineScope

internal class BusinessEventHandlerImpl(
    private val consumer: StandardEventConsumer,
) : EventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerReceiver(DeleteBusinessesForUserEvent.TOPIC) { event: DeleteBusinessesForUserEvent ->
            }
            .start(scope)
    }
}