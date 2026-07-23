package com.bookk.notifications.domain.impl.notification.renderer.appointment

import com.bookk.core.domain.entity.Language
import com.bookk.notifications.domain.impl.notification.EmailNotification
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.notifications.domain.impl.notification.PushNotification
import com.bookk.notifications.domain.impl.notification.TextNotification
import com.bookk.notifications.domain.impl.notification.renderer.formatLocalized
import com.bookk.server.appointments.client.api.event.AppointmentEvent

internal val AppointmentEvent.RequestRejected.notification: NotificationParameters
    get() = NotificationParameters(
        type = NotificationType.APPOINTMENT,
        push = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> PushNotification(
                    title = "Appointment declined",
                    body = "Your request for $businessName on $whenText was declined: $declineReason"
                )
                Language.UK -> PushNotification(
                    title = "Запис відхилено",
                    body = "Ваш запит до $businessName на $whenText відхилено: $declineReason"
                )
            }
        },
        email = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> EmailNotification(
                    subject = "Your request for $businessName was declined",
                    body = "Your request for $businessName on $whenText was declined: $declineReason. Address: $address."
                )
                Language.UK -> EmailNotification(
                    subject = "Ваш запит до $businessName відхилено",
                    body = "Ваш запит до $businessName на $whenText відхилено: $declineReason. Адреса: $address."
                )
            }
        },
        text = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> TextNotification("Your request for $businessName on $whenText was declined: $declineReason.")
                Language.UK -> TextNotification("Ваш запит до $businessName на $whenText відхилено: $declineReason.")
            }
        }
    )