package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.api.operation.IsAppointmentsEnabled
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
import io.ktor.client.plugins.resources.get
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

internal class EnableAppointmentsTest {

    private val testBusinessId = Uuid.random()

    @Test
    fun `should enable appointments successfully`() = routeTest {
        given()
        val useCase: EnableAppointmentsForBusiness = mockk()
        val userId = Uuid.random()
        val snapshot = BusinessSnapshot.stub(id = testBusinessId)

        coEvery { useCase.invoke(userId, snapshot) } returns Result.success(Unit)

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
                appointmentInit()
            }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Enabled(businessId = testBusinessId)) {
            setBody(snapshot)
        }

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return isEnabled status successfully`() = routeTest {
        given()
        val useCase: IsAppointmentsEnabled = mockk()
        val userId = Uuid.random()

        coEvery { useCase.invoke(userId, testBusinessId) } returns Result.success(true)

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
                appointmentInit()
            }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(Api.Appointment.Enabled(businessId = testBusinessId))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(true, response.body<Boolean>())
    }

    @Test
    fun `should return unprocessable entity when appointments already enabled`() = routeTest {
        given()
        val useCase: EnableAppointmentsForBusiness = mockk()
        val userId = Uuid.random()
        val snapshot = BusinessSnapshot.stub(id = testBusinessId)
        coEvery { useCase.invoke(userId, snapshot) } returns Result.failure(EnableAppointmentsForBusiness.Error.AlreadyEnabled())

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
            routeUnderTest = { appointmentInit() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Enabled(businessId = testBusinessId)) {
            setBody(snapshot)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.PLUGIN_ALREADY_ENABLED, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unauthorized when enabling appointments without authentication`() = routeTest {
        given()
        val useCase: EnableAppointmentsForBusiness = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { appointmentInit() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Enabled(businessId = testBusinessId)) {
            setBody(BusinessSnapshot.stub(id = testBusinessId))
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return unauthorized when checking if appointments are enabled without authentication`() = routeTest {
        given()
        val useCase: IsAppointmentsEnabled = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { appointmentInit() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(Api.Appointment.Enabled(businessId = testBusinessId))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
