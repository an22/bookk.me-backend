package com.bookk.notifications.microservice.route.api

import com.bookk.core.service.enity.respondWith
import com.bookk.notifications.domain.api.UpdatePushNotificationToken
import com.bookk.notifications.microservice.route.NotificationsRouting.Api
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
internal class UpdateTokenRequest(val token: String)

internal fun Route.updateToken() {
    authenticate {
        /**
         * Summary: Update push notification token
         * Description: Update the push notification token for a device
         * Tag: notification
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.notifications.microservice.route.api.UpdateTokenRequest]
         * Response: 200 application/x-protobuf [com.bookk.notifications.domain.api.entity.Device] Updated device
         */
        put<Api.Notification.Token> {
            val body = call.receive<UpdateTokenRequest>()
            val updatePushNotificationToken by application.inject<UpdatePushNotificationToken>()
            call.respondWith(updatePushNotificationToken(it.deviceUuid, body.token))
        }
    }
}
