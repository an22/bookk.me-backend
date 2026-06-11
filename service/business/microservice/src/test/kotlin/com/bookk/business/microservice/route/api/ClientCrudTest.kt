package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.toDomain
import com.bookk.business.domain.api.client.operation.CreateClient
import com.bookk.business.domain.api.client.operation.DeleteClient
import com.bookk.business.domain.api.client.operation.GetClients
import com.bookk.business.domain.api.error.BusinessErrorCodes
import com.bookk.business.microservice.route.BusinessRouting
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
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

internal class ClientCrudTest {

    private val businessId = Uuid.random()
    private val userId = Uuid.random()

    private fun createTestClientRemote() = ClientRemote(
        id = Uuid.random(),
        name = "John",
        lastName = "Doe",
        phone = "123456789",
        email = "john@example.com",
        userId = null
    )

    @Test
    fun `should create client`() = routeTest {
        given()
        val useCase: CreateClient = mockk()
        val clientRemote = createTestClientRemote()
        val client = clientRemote.toDomain()
        
        coEvery { useCase.invoke(businessId, client) } returns Result.success(clientRemote)
        
        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { clientCrud() }
        )
        
        whenn()
        val httpClient = createTestClient()
        val response = httpClient.post(BusinessRouting.Api.Clients(businessId = businessId)) {
            setBody(clientRemote)
        }
        
        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should return clients`() = routeTest {
        given()
        val useCase: GetClients = mockk()
        val clients = listOf(createTestClientRemote())
        
        coEvery { useCase.invoke(businessId) } returns Result.success(clients)
        
        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { clientCrud() }
        )
        
        whenn()
        val httpClient = createTestClient()
        val response = httpClient.get(BusinessRouting.Api.Clients(businessId = businessId))
        
        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should delete client`() = routeTest {
        given()
        val useCase: DeleteClient = mockk()
        val id = Uuid.random()

        coEvery { useCase.invoke(businessId, id) } returns Result.success(Unit)

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { clientCrud() }
        )

        whenn()
        val httpClient = createTestClient()
        val response = httpClient.delete(BusinessRouting.Api.Clients.Id(BusinessRouting.Api.Clients(businessId = businessId), id))

        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return unprocessable entity when client already exists`() = routeTest {
        given()
        val useCase: CreateClient = mockk()
        val clientRemote = createTestClientRemote()
        coEvery { useCase.invoke(businessId, clientRemote.toDomain()) } returns Result.failure(CreateClient.Error.ClientExist())

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module { single { useCase } },
            routeUnderTest = { clientCrud() }
        )

        whenn()
        val httpClient = createTestClient()
        val response = httpClient.post(BusinessRouting.Api.Clients(businessId = businessId)) {
            setBody(clientRemote)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_CLIENT_EXISTS, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when client validation error`() = routeTest {
        given()
        val useCase: CreateClient = mockk()
        val clientRemote = createTestClientRemote()
        coEvery { useCase.invoke(businessId, clientRemote.toDomain()) } returns Result.failure(CreateClient.Error.ClientValidationError())

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module { single { useCase } },
            routeUnderTest = { clientCrud() }
        )

        whenn()
        val httpClient = createTestClient()
        val response = httpClient.post(BusinessRouting.Api.Clients(businessId = businessId)) {
            setBody(clientRemote)
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_CLIENT_NAME_VALIDATION_ERROR, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return not found when deleting client that does not exist`() = routeTest {
        given()
        val useCase: DeleteClient = mockk()
        val id = Uuid.random()
        coEvery { useCase.invoke(businessId, id) } returns Result.failure(DeleteClient.Error.NotFound())

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module { single { useCase } },
            routeUnderTest = { clientCrud() }
        )

        whenn()
        val httpClient = createTestClient()
        val response = httpClient.delete(BusinessRouting.Api.Clients.Id(BusinessRouting.Api.Clients(businessId = businessId), id))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_CLIENT_NOT_EXISTS, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unauthorized when creating client without authentication`() = routeTest {
        given()
        val useCase: CreateClient = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { clientCrud() }
        )

        whenn()
        val httpClient = createTestClient()
        val response = httpClient.post(BusinessRouting.Api.Clients(businessId = businessId)) {
            setBody(createTestClientRemote())
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return unauthorized when getting clients without authentication`() = routeTest {
        given()
        val useCase: GetClients = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { clientCrud() }
        )

        whenn()
        val httpClient = createTestClient()
        val response = httpClient.get(BusinessRouting.Api.Clients(businessId = businessId))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return unauthorized when deleting client without authentication`() = routeTest {
        given()
        val useCase: DeleteClient = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { clientCrud() }
        )

        whenn()
        val httpClient = createTestClient()
        val response = httpClient.delete(BusinessRouting.Api.Clients.Id(BusinessRouting.Api.Clients(businessId = businessId), Uuid.random()))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
