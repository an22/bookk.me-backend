package com.book.user.microservice.route.api

import com.book.user.domain.api.routing.UserRouting.Api
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

internal fun Route.getHealthCheck() {
    withHealthCheckDocumentation()
    get<Api.User.HealthCheck> {
        call.respond(HttpStatusCode.OK)
    }
}

internal fun Route.withHealthCheckDocumentation() {
    install(NotarizedResource<Api.User.HealthCheck>()) {
        tags = setOf("user")
        post = PostInfo.builder {
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