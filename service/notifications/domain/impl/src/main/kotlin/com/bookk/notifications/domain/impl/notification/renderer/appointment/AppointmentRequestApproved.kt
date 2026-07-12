package com.bookk.notifications.domain.impl.notification.renderer.appointment

import com.bookk.notifications.domain.impl.notification.EmailNotification
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.notifications.domain.impl.notification.PushNotification
import com.bookk.notifications.domain.impl.notification.TextNotification
import com.bookk.server.appointments.client.api.event.AppointmentEvent

internal val AppointmentEvent.RequestApproved.notification: NotificationParameters
    get() = NotificationParameters(
        type = NotificationType.APPOINTMENT,
        push = PushNotification(
            title = "",
            body = "",
        ),
        email = EmailNotification(
            subject = "",
            body = ""
        ),
        text = TextNotification(
            ""
        )
    )