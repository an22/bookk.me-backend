package com.bookk.business.domain.impl.event

import com.bookk.business.domain.api.business.operation.DeleteBusiness
import com.bookk.business.domain.api.user.operation.SyncUserProfile
import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.registerResultReceiver
import com.bookk.server.auth.client.AuthEvent
import com.bookk.server.user.client.api.event.UserEvent
import kotlinx.coroutines.CoroutineScope

internal class BusinessEventHandlerImpl(
    private val consumer: StandardEventConsumer,
    private val deleteBusiness: DeleteBusiness,
    private val syncUserProfile: SyncUserProfile
) : EventHandler {
    override fun start(scope: CoroutineScope) {
        consumer
            .registerResultReceiver(AuthEvent.UserDeleted.TOPIC) { event: AuthEvent.UserDeleted ->
                deleteBusiness(event.userId)
            }
            .registerResultReceiver(UserEvent.Updated.TOPIC) { event: UserEvent.Updated ->
                syncUserProfile(
                    userId = event.userId,
                    name = event.name,
                    lastName = event.lastName,
                    phone = event.phone,
                    email = event.email
                )
            }
            .start(scope)
    }
}