package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.operation.DeclineAppointmentRequest
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal class DeclineAppointmentRequestTest {

    @Test
    fun `should decline request successfully`() = routeTest {
        given()
        val useCase: DeclineAppointmentRequest = mockk()
        val userId = Uuid.random()
        val businessId = Uuid.random()
        val requestId = Uuid.random()
        val cancellation = AppointmentCancellation(id = requestId, businessId = businessId, reason = "Reason")

        coEvery { useCase.invoke(userId, cancellation) } returns Result.success(Unit)

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
                requests()
            }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.RequestCancel(id = requestId)) {
            setBody(cancellation)
        }

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return unprocessable entity when already declined`() = routeTest {
        given()
        val useCase: DeclineAppointmentRequest = mockk()
        val userId = Uuid.random()
        val requestId = Uuid.random()
        val cancellation = AppointmentCancellation(id = requestId, businessId = Uuid.random(), reason = "Reason")

        coEvery {
            useCase.invoke(
                userId,
                cancellation
            )
        } returns Result.failure(DeclineAppointmentRequest.Error.AlreadyDeclined())

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
                requests()
            }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.RequestCancel(id = requestId)) {
            setBody(cancellation)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = response.body<SimpleServerError>()
        assertEquals(AppointmentErrorCodes.REQUEST_ALREADY_DECLINED, body.errorCode)
    }

    @Test
    fun `should return unprocessable entity when already approved`() = routeTest {
        given()
        val useCase: DeclineAppointmentRequest = mockk()
        val userId = Uuid.random()
        val requestId = Uuid.random()
        val cancellation = AppointmentCancellation(id = requestId, businessId = Uuid.random(), reason = "Reason")

        coEvery {
            useCase.invoke(
                userId,
                cancellation
            )
        } returns Result.failure(DeclineAppointmentRequest.Error.AlreadyApproved())

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
                requests()
            }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.RequestCancel(id = requestId)) {
            setBody(cancellation)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = response.body<SimpleServerError>()
        assertEquals(AppointmentErrorCodes.REQUEST_ALREADY_APPROVED, body.errorCode)
    }

    @Test
    fun `should return unauthorized when declining request without authentication`() = routeTest {
        given()
        val useCase: DeclineAppointmentRequest = mockk()

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
                requests()
            }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.RequestCancel(id = Uuid.random())) {
            setBody(AppointmentCancellation(id = Uuid.random(), businessId = Uuid.random(), reason = "Reason"))
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
