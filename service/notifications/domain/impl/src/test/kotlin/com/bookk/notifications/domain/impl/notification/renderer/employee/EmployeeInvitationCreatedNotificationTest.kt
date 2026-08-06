package com.bookk.notifications.domain.impl.notification.renderer.employee

import com.bookk.core.domain.entity.Language
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.notifications.domain.impl.notification.NotificationType
import com.bookk.server.business.client.api.event.BusinessEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class EmployeeInvitationCreatedNotificationTest {

    private fun event() = BusinessEvent.EmployeeInvitationCreated(
        invitedUserId = Uuid.random(),
        businessId = Uuid.random(),
        businessName = "Barbershop"
    )

    @Test
    fun `should be rendered as an employee notification type`() = runUnitTest {
        given()
        val event = event()

        whenn()
        val type = event.notification.type

        then()
        assertEquals(NotificationType.EMPLOYEE, type)
    }

    @Test
    fun `should render English push content`() = runUnitTest {
        given()
        val event = event()

        whenn()
        val push = event.notification.push(Language.EN)

        then()
        assertEquals("New employee invitation", push.title)
        assertEquals("You were invited to join Barbershop as an employee", push.body)
    }

    @Test
    fun `should render Ukrainian push content`() = runUnitTest {
        given()
        val event = event()

        whenn()
        val push = event.notification.push(Language.UK)

        then()
        assertEquals("Нове запрошення до команди", push.title)
        assertEquals("Вас запросили приєднатися до Barbershop як працівника", push.body)
    }

    @Test
    fun `should render English email content`() = runUnitTest {
        given()
        val event = event()

        whenn()
        val email = event.notification.email(Language.EN)

        then()
        assertEquals("You were invited to join Barbershop", email.subject)
        assertEquals(
            "You were invited to join Barbershop as an employee. Open the app to accept the invitation.",
            email.body
        )
    }

    @Test
    fun `should render Ukrainian email content`() = runUnitTest {
        given()
        val event = event()

        whenn()
        val email = event.notification.email(Language.UK)

        then()
        assertEquals("Вас запросили приєднатися до Barbershop", email.subject)
        assertEquals(
            "Вас запросили приєднатися до Barbershop як працівника. Відкрийте застосунок, щоб прийняти запрошення.",
            email.body
        )
    }

    @Test
    fun `should render English text content`() = runUnitTest {
        given()
        val event = event()

        whenn()
        val text = event.notification.text(Language.EN)

        then()
        assertEquals("You were invited to join Barbershop as an employee.", text.text)
    }

    @Test
    fun `should render Ukrainian text content`() = runUnitTest {
        given()
        val event = event()

        whenn()
        val text = event.notification.text(Language.UK)

        then()
        assertEquals("Вас запросили приєднатися до Barbershop як працівника.", text.text)
    }
}
