package com.bookk.appointments.domain.impl.event

import com.bookk.appointments.domain.api.operation.DeleteModule
import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.registerReceiver
import com.bookk.server.business.client.api.event.BusinessEvent
import kotlinx.coroutines.CoroutineScope

internal class AppointmentEventHandler(
    private val consumer: StandardEventConsumer,
    private val deleteModule: DeleteModule
) : EventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerReceiver(BusinessEvent.Deleted.TOPIC) { event: BusinessEvent.Deleted ->
                deleteModule(event.businessId)
            }
            .start(scope)
    }
}