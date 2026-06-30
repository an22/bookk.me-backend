package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.entity.AppointmentOffer
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
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
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.success(Unit)

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
            setBody(offer)
        }

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return unprocessable entity when request already exists`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(CreateAppointmentRequest.Error.RequestForThisTimeExists())

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
            setBody(offer)
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
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed())

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
            setBody(offer)
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
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(CreateAppointmentRequest.Error.RequestForThisDateNotAllowed())

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
            setBody(offer)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.DATE_NOT_ALLOWED, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when date is in the past`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(CreateAppointmentRequest.Error.DateInThePastNotAllowed())

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
            setBody(offer)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.DATE_IN_PAST, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return bad request when price changed`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(CreateAppointmentRequest.Error.PriceChanged())

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
            setBody(offer)
        }

        then()
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(AppointmentErrorCodes.PRICE_CHANGED, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return bad request when services signature does not match`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(CreateAppointmentRequest.Error.ServicesSignatureMiss())

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
            setBody(offer)
        }

        then()
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(AppointmentErrorCodes.SERVICES_VALIDATION_FAILED, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when token already used`() = routeTest {
        given()
        val useCase: CreateAppointmentRequest = mockk()
        val userId = Uuid.random()
        val offer = AppointmentOffer.stub(request = AppointmentRequest.stub(userId = userId))
        coEvery { useCase.invoke(userId, any()) } returns Result.failure(CreateAppointmentRequest.Error.TokenAlreadyUsed())

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
            setBody(offer)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.QUOTE_TOKEN_ALREADY_USED, response.body<SimpleServerError>().errorCode)
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
            setBody(AppointmentOffer.stub())
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
