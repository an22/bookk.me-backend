package com.bookk.auth.microservice.route.api

import com.bookk.auth.microservice.route.AuthRouting.Api
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

internal fun Route.healthCheck() {
    /**
     * Healthcheck
     * @description Check service health
     * @tag *auth
     * @response 200 Service is alive
     */
    get<Api.Auth.HealthCheck> {
        call.respond(HttpStatusCode.OK)
    }
}
