package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.microservice.route.AppointmentsRouting
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.mockk.coEvery
import io.mockk.mockk
import org.joda.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class CreateAppointmentTest {

    @Test
    fun `should create appointment successfully`() = routeTest {
        given()
        val useCase: CreateAppointment = mockk()
        val requestId = Uuid.random()
        val userId = Uuid.random()
        val appointment = Appointment(
            id = Uuid.random(),
            userId = userId,
            businessId = Uuid.random(),
            client = ClientSnapshot(Uuid.random(), "Full Name", "123456789", "test@example.com"),
            service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.parse("USD 100"), duration = 30.minutes),
            date = Instant.fromEpochMilliseconds(0),
            note = "test"
        )

        coEvery { useCase.invoke(userId, requestId) } returns Result.success(appointment)

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { context ->
                            context.principal(AppPrincipal(Uuid.random(), userId, Uuid.random()))
                        }
                    }
                }
            },
            diModule = module {
                single { useCase }
            },
            routeUnderTest = {
                appointment()
            }
        )
        
        whenn()
        val client = createTestClient()
        val response = client.post(AppointmentsRouting.Api.Appointment()) {
            setBody(AppointmentRequestId(requestId))
        }

        then()
        assertEquals(HttpStatusCode.OK, response.status, "Response status: ${response.status}")
    }
}
