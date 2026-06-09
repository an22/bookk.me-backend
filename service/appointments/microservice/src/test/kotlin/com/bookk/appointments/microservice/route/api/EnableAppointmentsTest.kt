package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
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
        val snapshot = BusinessSnapshot.stub().copy(id = testBusinessId)

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
        val response = client.post(Api.Appointment.Enable(businessId = testBusinessId)) {
            setBody(snapshot)
        }

        then()
        assertEquals(HttpStatusCode.NoContent, response.status, "Response body: ${response.bodyAsText()}")
    }

    @Test
    fun `should return unauthorized when enabling without authentication`() = routeTest {
        given()
        val useCase: EnableAppointmentsForBusiness = mockk()
        val snapshot = BusinessSnapshot.stub().copy(id = testBusinessId)

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
                appointmentInit()
            }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(Api.Appointment.Enable(businessId = testBusinessId)) {
            setBody(snapshot)
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
