package com.bookk.notifications.domain.impl.notification.renderer.employee

import com.bookk.notifications.domain.impl.notification.EmailNotification
import com.bookk.notifications.domain.impl.notification.NotificationParameters
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.notifications.domain.impl.notification.PushNotification
import com.bookk.notifications.domain.impl.notification.TextNotification
import com.bookk.notifications.domain.impl.notification.renderer.message
import com.bookk.server.business.client.api.event.BusinessEvent

private const val KEY_PREFIX = "employee.invitation_created"

internal val BusinessEvent.EmployeeInvitationCreated.notification: NotificationParameters
    get() = NotificationParameters(
        type = NotificationType.EMPLOYEE,
        push = { language ->
            PushNotification(
                title = message(language, "$KEY_PREFIX.push.title"),
                body = message(language, "$KEY_PREFIX.push.body", businessName)
            )
        },
        email = { language ->
            EmailNotification(
                subject = message(language, "$KEY_PREFIX.email.subject", businessName),
                body = message(language, "$KEY_PREFIX.email.body", businessName)
            )
        },
        text = { language ->
            TextNotification(message(language, "$KEY_PREFIX.text.body", businessName))
        }
    )
