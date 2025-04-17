package com.book.business.microservice.route

import io.ktor.resources.Resource

object BusinessRouting {
    @Resource("api")
    class Api {

        @Resource("/business")
        class Business(val parent: Api = Api()) {

            @Resource("/healthcheck")
            class HealthCheck(val parent: Business = Business())
        }
    }
}