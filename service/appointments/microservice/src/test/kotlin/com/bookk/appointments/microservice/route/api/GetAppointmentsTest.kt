package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.api.operation.GetAppointments
import com.bookk.appointments.microservice.route.AppointmentsRouting
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.mockk.coEvery
import io.mockk.mockk
import org.joda.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class GetAppointmentsTest {

    @Test
    fun `should get appointments successfully`() = routeTest {
        given()
        val useCase: GetAppointments = mockk()
        val businessId = Uuid.random()
        val userId = Uuid.random()
        val appointments = listOf(
            Appointment(
                id = Uuid.random(),
                userId = userId,
                businessId = businessId,
                client = ClientSnapshot(Uuid.random(), "Full Name", "123456789", "test@example.com"),
                service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.parse("USD 100"), duration = 30.minutes),
                date = Instant.fromEpochMilliseconds(0),
                note = "test"
            )
        )

        coEvery { useCase.invoke(userId, businessId) } returns Result.success(appointments)

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
        val response = client.get(AppointmentsRouting.Api.Appointments(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should return unauthorized when getting appointments without authentication`() = routeTest {
        given()
        val useCase: GetAppointments = mockk()
        val businessId = Uuid.random()
        val userId = Uuid.random()
        val appointments = listOf(
            Appointment(
                id = Uuid.random(),
                userId = userId,
                businessId = businessId,
                client = ClientSnapshot(Uuid.random(), "Full Name", "123456789", "test@example.com"),
                service = ServiceSnapshot(Uuid.random(), "Service Name", Uuid.random(), Money.parse("USD 100"), duration = 30.minutes),
                date = Instant.fromEpochMilliseconds(0),
                note = "test"
            )
        )
        coEvery { useCase.invoke(userId, businessId) } returns Result.success(appointments)

        setupApplication(
            extension = {
                install(Authentication) {
                    bearer {
                        authenticate { null }
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
        val response = client.get(AppointmentsRouting.Api.Appointments(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
