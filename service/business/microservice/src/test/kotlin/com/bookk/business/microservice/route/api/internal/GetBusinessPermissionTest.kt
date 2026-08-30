package com.bookk.business.microservice.route.api.internal

import com.bookk.business.domain.api.business.operation.GetBusinessPermission
import com.bookk.business.microservice.route.BusinessRouting
import com.bookk.core.domain.entity.Error
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import library.permissions.ObjectPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal class GetBusinessPermissionTest {

    private val userId = Uuid.random()
    private val businessId = Uuid.random()

    private fun permissionsResource() = BusinessRouting.Api.Internal.Business.Id.Permissions(
        parent = BusinessRouting.Api.Internal.Business.Id(id = businessId),
        userId = userId
    )

    @Test
    fun `should return the permission the user holds`() = routeTest {
        given()
        val useCase: GetBusinessPermission = mockk()
        coEvery { useCase.invoke(userId, businessId) } returns Result.success(ObjectPermission.OWNER)

        setupApplication(
            diModule = module { single { useCase } },
            routeUnderTest = { getBusinessPermission() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(permissionsResource())

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ObjectPermission.OWNER, response.body<ObjectPermission>())
    }

    @Test
    fun `should return no permission when the user holds none`() = routeTest {
        given()
        val useCase: GetBusinessPermission = mockk()
        coEvery { useCase.invoke(userId, businessId) } returns Result.success(ObjectPermission.NONE)

        setupApplication(
            diModule = module { single { useCase } },
            routeUnderTest = { getBusinessPermission() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(permissionsResource())

        then()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ObjectPermission.NONE, response.body<ObjectPermission>())
    }

    @Test
    fun `should return not found when the business is unknown`() = routeTest {
        given()
        val useCase: GetBusinessPermission = mockk()
        coEvery { useCase.invoke(userId, businessId) } returns Result.failure(Error.NotFound())

        setupApplication(
            diModule = module { single { useCase } },
            routeUnderTest = { getBusinessPermission() }
        )

        whenn()
        val client = createTestClient()
        val response = client.get(permissionsResource())

        then()
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
