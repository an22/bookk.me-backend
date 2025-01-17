package com.book.auth.microservice.route.api

import com.book.auth.microservice.route.AuthRouting.Api
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

internal fun Route.healthCheck() {
    withHealthCheckDocumentation()
    get<Api.Auth.HealthCheck> {
        call.respond(HttpStatusCode.OK)
    }
}

internal fun Route.withHealthCheckDocumentation() {
    install(NotarizedResource<Api.Auth.HealthCheck>()) {
        tags = setOf("auth")
        get = GetInfo.builder {
            summary("Healthcheck")
            description("Check service health")
            response {
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("Service is alive")
            }
        }
    }
}
