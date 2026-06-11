package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
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

internal class CreateAppointmentRequestTest {

    @Test
    fun `should create appointment request successfully`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId)
        coEvery { useCase.invoke(userId, request) } returns Result.success(Unit)

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
            diModule = module { single { useCase } },
            routeUnderTest = { requests() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Request()) {
            setBody(request)
        }

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return unprocessable entity when request already exists`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId)
        coEvery { useCase.invoke(userId, request) } returns Result.failure(CreateAppointmentRequest.Error.RequestForThisTimeExists())

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
            diModule = module { single { useCase } },
            routeUnderTest = { requests() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Request()) {
            setBody(request)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.REQUEST_EXISTS, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when time not allowed`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId)
        coEvery { useCase.invoke(userId, request) } returns Result.failure(CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed())

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
            diModule = module { single { useCase } },
            routeUnderTest = { requests() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Request()) {
            setBody(request)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.TIME_NOT_ALLOWED, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when date not allowed`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val request = AppointmentRequest.stub(userId = userId)
        coEvery { useCase.invoke(userId, request) } returns Result.failure(CreateAppointmentRequest.Error.RequestForThisDateNotAllowed())

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
            diModule = module { single { useCase } },
            routeUnderTest = { requests() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Request()) {
            setBody(request)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.DATE_NOT_ALLOWED, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unauthorized when creating appointment request without authentication`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { requests() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Request()) {
            setBody(AppointmentRequest.stub())
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
