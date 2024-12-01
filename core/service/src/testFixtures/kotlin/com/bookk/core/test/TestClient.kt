package com.bookk.core.test

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json


fun ApplicationTestBuilder.createTestClient(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    encodeDefaults = true
                    explicitNulls = false
                }
            )
        }
        install(Resources)
    }
}