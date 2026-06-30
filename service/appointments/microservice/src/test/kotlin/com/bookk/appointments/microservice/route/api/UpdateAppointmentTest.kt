package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.appointments.domain.api.operation.UpdateAppointment
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
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

internal class UpdateAppointmentTest {

    private val testAppointment = Appointment(
        id = Uuid.random(),
        userId = Uuid.random(),
        businessId = Uuid.random(),
        client = ClientSnapshot(Uuid.random(), "Name", "123", "a@b.com"),
        services = listOf(ServiceSnapshot(Uuid.random(), "Svc", Uuid.random(), Money.parse("USD 100"), 30.minutes)),
        date = Instant.fromEpochMilliseconds(0),
        note = "Note",
        status = AppointmentStatus.SCHEDULED,
        cancellationReason = ""
    )

    @Test
    fun `should update appointment successfully`() = routeTest {
        given()
        val useCase: UpdateAppointment = mockk()
        val userId = Uuid.random()

        coEvery { useCase.invoke(userId, any()) } returns Result.success(testAppointment)

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
        val response = client.put(Api.Appointment.Id(id = testAppointment.id)) {
            setBody(testAppointment)
        }

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(testAppointment, response.body<Appointment>())
    }

    @Test
    fun `should return unauthorized when updating appointment without authentication`() = routeTest {
        given()
        val useCase: UpdateAppointment = mockk()

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
        val response = client.put(Api.Appointment.Id(id = testAppointment.id)) {
            setBody(testAppointment)
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return unprocessable entity when appointment exists`() = routeTest {
        given()
        val useCase: UpdateAppointment = mockk()
        val userId = Uuid.random()

        coEvery {
            useCase.invoke(
                userId,
                any()
            )
        } returns Result.failure(UpdateAppointment.Error.AppointmentForThisTimeExists())

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
        val response = client.put(Api.Appointment.Id(id = testAppointment.id)) {
            setBody(testAppointment)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = response.body<SimpleServerError>()
        assertEquals(AppointmentErrorCodes.APPOINTMENT_EXISTS, body.errorCode)
    }

    @Test
    fun `should return unprocessable entity when time not allowed`() = routeTest {
        given()
        val useCase: UpdateAppointment = mockk()
        val userId = Uuid.random()

        coEvery {
            useCase.invoke(
                userId,
                any()
            )
        } returns Result.failure(UpdateAppointment.Error.RequestForThisTimeNotAllowed())

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
        val response = client.put(Api.Appointment.Id(id = testAppointment.id)) {
            setBody(testAppointment)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = response.body<SimpleServerError>()
        assertEquals(AppointmentErrorCodes.TIME_NOT_ALLOWED, body.errorCode)
    }

    @Test
    fun `should return unprocessable entity when date not allowed`() = routeTest {
        given()
        val useCase: UpdateAppointment = mockk()
        val userId = Uuid.random()

        coEvery {
            useCase.invoke(
                userId,
                any()
            )
        } returns Result.failure(UpdateAppointment.Error.RequestForThisDateNotAllowed())

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
        val response = client.put(Api.Appointment.Id(id = testAppointment.id)) {
            setBody(testAppointment)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = response.body<SimpleServerError>()
        assertEquals(AppointmentErrorCodes.DATE_NOT_ALLOWED, body.errorCode)
    }
}
