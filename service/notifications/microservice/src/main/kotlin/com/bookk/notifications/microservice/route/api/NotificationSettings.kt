package com.bookk.notifications.microservice.route.api

import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import com.bookk.notifications.domain.api.GetNotificationSettings
import com.bookk.notifications.domain.api.UpdateNotificationSettings
import com.bookk.notifications.domain.api.entity.NotificationChannelSettings
import com.bookk.notifications.microservice.route.NotificationsRouting.Api
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
internal class UpdateNotificationSettingsRequest(
    val appointmentEnabled: Boolean,
    val channels: List<NotificationChannelSettings>,
)

internal fun Route.notificationSettings() {
    authenticate {
        /**
         * Summary: Get notification settings
         * Description: Get notification settings for authenticated user
         * Tag: notification
         * Security: jwt
         * Response: 200 application/x-protobuf [com.bookk.notifications.domain.api.entity.NotificationSettings] Notification settings
         */
        get<Api.Notification.Settings> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getNotificationSettings by application.inject<GetNotificationSettings>()
            call.respondWith(getNotificationSettings(principal.userId))
        }

        /**
         * Summary: Update notification settings
         * Description: Update notification settings for authenticated user
         * Tag: notification
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.notifications.microservice.route.api.UpdateNotificationSettingsRequest]
         * Response: 200 application/x-protobuf [com.bookk.notifications.domain.api.entity.NotificationSettings] Updated notification settings
         */
        put<Api.Notification.Settings> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<UpdateNotificationSettingsRequest>()
            val updateNotificationSettings by application.inject<UpdateNotificationSettings>()
            call.respondWith(updateNotificationSettings(principal.userId, body.appointmentEnabled, body.channels))
        }
    }
}
