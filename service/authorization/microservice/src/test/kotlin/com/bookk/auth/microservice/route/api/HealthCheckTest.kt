package com.bookk.auth.microservice.route.api

import com.bookk.auth.microservice.route.AuthRouting
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.installTestPlugins
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HealthCheckTest {
    @Test
    fun test() = testApplication {
        application {
            installTestPlugins()
            routing {
                healthCheck()
            }
        }
        val client = createTestClient()
        val response = client.get(AuthRouting.Api.Auth.HealthCheck())
        assertEquals(HttpStatusCode.OK, response.status)
    }
}