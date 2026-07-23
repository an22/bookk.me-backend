package com.bookk.notifications.domain.impl.notification.renderer.appointment

import com.bookk.core.domain.entity.Language
import com.bookk.notifications.domain.impl.notification.EmailNotification
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.notifications.domain.impl.notification.PushNotification
import com.bookk.notifications.domain.impl.notification.TextNotification
import com.bookk.notifications.domain.impl.notification.renderer.formatLocalized
import com.bookk.server.appointments.client.api.event.AppointmentEvent

internal val AppointmentEvent.RequestCreated.notification: NotificationParameters
    get() = NotificationParameters(
        type = NotificationType.APPOINTMENT,
        push = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> PushNotification(
                    title = "New appointment request",
                    body = "$clientName wants to book $businessName on $whenText"
                )
                Language.UK -> PushNotification(
                    title = "Новий запит на запис",
                    body = "$clientName хоче записатися до $businessName на $whenText"
                )
            }
        },
        email = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> EmailNotification(
                    subject = "New appointment request from $clientName",
                    body = "$clientName has requested an appointment at $businessName on $whenText. Address: $address. Price: $price."
                )
                Language.UK -> EmailNotification(
                    subject = "Новий запит на запис від $clientName",
                    body = "$clientName залишив(ла) запит на запис до $businessName на $whenText. Адреса: $address. Вартість: $price."
                )
            }
        },
        text = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> TextNotification("$clientName requested an appointment at $businessName on $whenText.")
                Language.UK -> TextNotification("$clientName запросив(ла) запис до $businessName на $whenText.")
            }
        }
    )