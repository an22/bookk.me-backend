package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.operation.GetAppointmentsForDate
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
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal class GetAppointmentsForDateTest {

    @Test
    fun `should get appointments successfully`() = routeTest {
        given()
        val useCase: GetAppointmentsForDate = mockk()
        val businessId = Uuid.random()
        val userId = Uuid.random()
        val date = LocalDate(2024, 1, 15)
        val appointments = listOf(Appointment.stub(userId = userId, businessId = businessId))

        coEvery { useCase.invoke(userId, businessId, date) } returns Result.success(appointments)

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
        val response = client.get(AppointmentsRouting.Api.Appointments(businessId = businessId, date = date))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should return unauthorized when getting appointments without authentication`() = routeTest {
        given()
        val useCase: GetAppointmentsForDate = mockk()
        val businessId = Uuid.random()
        val date = LocalDate(2024, 1, 15)

        setupApplication(
            extension = {
                install(Authentication) {
                    bearer { authenticate { null } }
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
        val response = client.get(AppointmentsRouting.Api.Appointments(businessId = businessId, date = date))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return server error when getting appointments fails`() = routeTest {
        given()
        val useCase: GetAppointmentsForDate = mockk()
        val businessId = Uuid.random()
        val userId = Uuid.random()
        val date = LocalDate(2024, 1, 15)

        coEvery { useCase.invoke(userId, businessId, date) } returns Result.failure(Exception("Database error"))

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
        val response = client.get(AppointmentsRouting.Api.Appointments(businessId = businessId, date = date))

        then()
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
}
