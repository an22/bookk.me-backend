package com.book.business.domain.impl.event

import com.book.core.data.eventstreaming.EventHandler
import com.book.core.data.eventstreaming.StandardEventConsumer
import com.book.core.data.eventstreaming.registerReceiver
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