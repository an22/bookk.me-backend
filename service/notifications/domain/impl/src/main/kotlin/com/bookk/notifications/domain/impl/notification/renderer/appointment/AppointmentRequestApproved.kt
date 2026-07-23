package com.bookk.notifications.domain.impl.notification.renderer.appointment

import com.bookk.core.domain.entity.Language
import com.bookk.notifications.domain.impl.notification.EmailNotification
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.notifications.domain.impl.notification.PushNotification
import com.bookk.notifications.domain.impl.notification.TextNotification
import com.bookk.notifications.domain.impl.notification.renderer.formatLocalized
import com.bookk.server.appointments.client.api.event.AppointmentEvent

internal val AppointmentEvent.RequestApproved.notification: NotificationParameters
    get() = NotificationParameters(
        type = NotificationType.APPOINTMENT,
        push = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> PushNotification(
                    title = "Appointment confirmed",
                    body = "Your appointment at $businessName on $whenText is confirmed"
                )
                Language.UK -> PushNotification(
                    title = "Запис підтверджено",
                    body = "Ваш запис до $businessName на $whenText підтверджено"
                )
            }
        },
        email = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> EmailNotification(
                    subject = "Your appointment at $businessName is confirmed",
                    body = "Your appointment at $businessName on $whenText is confirmed. Address: $address. Price: $price."
                )
                Language.UK -> EmailNotification(
                    subject = "Ваш запис до $businessName підтверджено",
                    body = "Ваш запис до $businessName на $whenText підтверджено. Адреса: $address. Вартість: $price."
                )
            }
        },
        text = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> TextNotification("Your appointment at $businessName on $whenText is confirmed.")
                Language.UK -> TextNotification("Ваш запис до $businessName на $whenText підтверджено.")
            }
        }
    )