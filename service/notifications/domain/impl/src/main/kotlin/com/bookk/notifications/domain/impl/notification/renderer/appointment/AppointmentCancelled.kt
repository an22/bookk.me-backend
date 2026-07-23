package com.bookk.notifications.domain.impl.notification.renderer.appointment

import com.bookk.core.domain.entity.Language
import com.bookk.notifications.domain.impl.notification.EmailNotification
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.notifications.domain.impl.notification.PushNotification
import com.bookk.notifications.domain.impl.notification.TextNotification
import com.bookk.notifications.domain.impl.notification.renderer.formatLocalized
import com.bookk.server.appointments.client.api.event.AppointmentEvent

internal val AppointmentEvent.Cancelled.notification: NotificationParameters
    get() = NotificationParameters(
        type = NotificationType.APPOINTMENT,
        push = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> PushNotification(
                    title = "Appointment cancelled",
                    body = "Your appointment at $businessName on $whenText was cancelled: $reason"
                )
                Language.UK -> PushNotification(
                    title = "Запис скасовано",
                    body = "Ваш запис до $businessName на $whenText скасовано: $reason"
                )
            }
        },
        email = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> EmailNotification(
                    subject = "Your appointment at $businessName was cancelled",
                    body = "Your appointment at $businessName on $whenText was cancelled: $reason. Address: $address."
                )
                Language.UK -> EmailNotification(
                    subject = "Ваш запис до $businessName скасовано",
                    body = "Ваш запис до $businessName на $whenText скасовано: $reason. Адреса: $address."
                )
            }
        },
        text = { language ->
            val whenText = from.formatLocalized(timeZone, language)
            when (language) {
                Language.EN -> TextNotification("Your appointment at $businessName on $whenText was cancelled: $reason.")
                Language.UK -> TextNotification("Ваш запис до $businessName на $whenText скасовано: $reason.")
            }
        }
    )
