package com.bookk.user.microservice.route.api


import com.bookk.user.microservice.route.UserRouting.Api
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

internal fun Route.getHealthCheck() {
    /**
     * Healthcheck
     * @description Check service health
     * @tag *user
     * @response 200 Service is alive
     */
    get<Api.User.HealthCheck> {
        call.respond(HttpStatusCode.OK)
    }
}