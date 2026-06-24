package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentErrorCodes
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.operation.EditSettings
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
import io.ktor.client.plugins.resources.put
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

internal class EditSettingsTest {

    private val testBusinessId = Uuid.random()

    @Test
    fun `should update settings successfully`() = routeTest {
        given()
        val useCase: EditSettings = mockk()
        val userId = Uuid.random()
        val appointmentSettings = AppointmentSettings.stub(businessId = testBusinessId)
        coEvery { useCase.invoke(userId, appointmentSettings) } returns Result.success(appointmentSettings)

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
            routeUnderTest = { settings() }
        )

        whenn()
        val client = createTestClient()
        val response = client.put(Api.Appointment.Settings(businessId = testBusinessId)) {
            setBody(appointmentSettings)
        }

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(appointmentSettings, response.body<AppointmentSettings>())
    }

    @Test
    fun `should return unprocessable entity when active day has no work hours`() = routeTest {
        given()
        val useCase: EditSettings = mockk()
        val userId = Uuid.random()
        val appointmentSettings = AppointmentSettings.stub(businessId = testBusinessId)
        coEvery { useCase.invoke(userId, appointmentSettings) } returns
            Result.failure(EditSettings.Error.ActiveDayWithoutWorkHours())

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
            routeUnderTest = { settings() }
        )

        whenn()
        val client = createTestClient()
        val response = client.put(Api.Appointment.Settings(businessId = testBusinessId)) {
            setBody(appointmentSettings)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.ACTIVE_DAY_WITHOUT_WORK_HOURS, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when day off range start date is not before end date`() = routeTest {
        given()
        val useCase: EditSettings = mockk()
        val userId = Uuid.random()
        val appointmentSettings = AppointmentSettings.stub(businessId = testBusinessId)
        coEvery { useCase.invoke(userId, appointmentSettings) } returns
            Result.failure(EditSettings.Error.InvalidDayOffRange())

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
            routeUnderTest = { settings() }
        )

        whenn()
        val client = createTestClient()
        val response = client.put(Api.Appointment.Settings(businessId = testBusinessId)) {
            setBody(appointmentSettings)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AppointmentErrorCodes.INVALID_DAY_OFF_RANGE, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unauthorized when updating settings without authentication`() = routeTest {
        given()
        val useCase: EditSettings = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { settings() }
        )

        whenn()
        val client = createTestClient()
        val response = client.put(Api.Appointment.Settings(businessId = testBusinessId)) {
            setBody(AppointmentSettings.stub(businessId = testBusinessId))
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
