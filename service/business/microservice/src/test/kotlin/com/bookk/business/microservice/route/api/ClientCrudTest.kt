package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.toDomain
import com.bookk.business.domain.api.client.operation.CreateClient
import com.bookk.business.domain.api.client.operation.DeleteClient
import com.bookk.business.domain.api.client.operation.GetClients
import com.bookk.business.microservice.route.BusinessRouting
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
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
}
