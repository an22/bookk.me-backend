package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.entity.UserBusinesses
import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.api.business.operation.GetUserBusinesses
import com.bookk.business.domain.api.business.operation.UpdateBusiness
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
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
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

internal class BusinessCrudTest {

    private val userId = Uuid.random()
    private val businessId = Uuid.random()

    private fun createTestBusiness(id: Uuid = businessId) = Business.stub(
        id = id,
        name = "Test Business",
        description = "Test Description",
        address = "Test Address"
    )

    @Test
    fun `should create business`() = routeTest {
        given()
        val useCase: CreateBusiness = mockk()
        val business = createTestBusiness()
        val name = "Test Business"
        val currencyCode = "USD"
        
        coEvery { useCase.invoke(userId, name, currencyCode) } returns Result.success(business)
        
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
            routeUnderTest = { businessCrud() }
        )
        
        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.Business()) {
            setBody(BusinessCreateRequest(name, currencyCode))
        }
        
        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should update business`() = routeTest {
        given()
        val useCase: UpdateBusiness = mockk()
        val updateModel = BusinessUpdateModel(businessId, "New Name", null, null, null, null, null, emptyList())
        
        coEvery { useCase.invoke(updateModel) } returns Result.success(Unit)
        
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
            routeUnderTest = { businessCrud() }
        )
        
        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Business.Id(id = businessId)) {
            setBody(updateModel)
        }
        
        then()
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `should return user businesses`() = routeTest {
        given()
        val useCase: GetUserBusinesses = mockk()
        val userBusinesses = UserBusinesses(businessId, listOf(createTestBusiness()))
        
        coEvery { useCase.invoke(userId) } returns Result.success(userBusinesses)
        
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
            routeUnderTest = { businessCrud() }
        )
        
        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Business())
        
        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should return business by id`() = routeTest {
        given()
        val useCase: GetBusinessById = mockk()
        val business = createTestBusiness()

        coEvery { useCase.invoke(businessId) } returns Result.success(business)

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
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Business.Id(id = businessId))

        then()
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should return unprocessable entity when business already exists`() = routeTest {
        given()
        val useCase: CreateBusiness = mockk()
        coEvery { useCase.invoke(userId, any(), any()) } returns Result.failure(CreateBusiness.Error.BusinessExist())

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module { single { useCase } },
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.Business()) {
            setBody(BusinessCreateRequest("Name", "USD"))
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_ALREADY_EXIST, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unprocessable entity when business validation error`() = routeTest {
        given()
        val useCase: CreateBusiness = mockk()
        coEvery { useCase.invoke(userId, any(), any()) } returns Result.failure(CreateBusiness.Error.BusinessValidationError())

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module { single { useCase } },
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.Business()) {
            setBody(BusinessCreateRequest("Name", "USD"))
        }

        then()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_NAME_VALIDATION_ERROR, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return not found when getting business by id that does not exist`() = routeTest {
        given()
        val useCase: GetBusinessById = mockk()
        coEvery { useCase.invoke(businessId) } returns Result.failure(GetBusinessById.Error.NotFound())

        setupApplication(
            extension = {
                install(Authentication) {
                    provider {
                        authenticate { it.principal(AppPrincipal(Uuid.random(), userId, Uuid.random())) }
                    }
                }
            },
            diModule = module { single { useCase } },
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Business.Id(id = businessId))

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(BusinessErrorCodes.BUSINESS_NOT_FOUND, response.body<SimpleServerError>().errorCode)
    }

    @Test
    fun `should return unauthorized when creating business without authentication`() = routeTest {
        given()
        val useCase: CreateBusiness = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.post(BusinessRouting.Api.Business()) {
            setBody(BusinessCreateRequest("Name", "USD"))
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return unauthorized when getting businesses without authentication`() = routeTest {
        given()
        val useCase: GetUserBusinesses = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Business())

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return unauthorized when getting business by id without authentication`() = routeTest {
        given()
        val useCase: GetBusinessById = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(BusinessRouting.Api.Business.Id(id = businessId))

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should return unauthorized when updating business without authentication`() = routeTest {
        given()
        val useCase: UpdateBusiness = mockk()

        setupApplication(
            extension = { install(Authentication) { bearer { authenticate { null } } } },
            diModule = module { single { useCase } },
            routeUnderTest = { businessCrud() }
        )

        whenn()
        val client = createTestClient()
        val response = client.put(BusinessRouting.Api.Business.Id(id = businessId)) {
            setBody(BusinessUpdateModel(businessId, null, null, null, null, null, null, emptyList()))
        }

        then()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
