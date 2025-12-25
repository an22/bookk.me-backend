package com.bookk.business.microservice.route

import io.ktor.resources.Resource
import kotlin.uuid.Uuid

object BusinessRouting {
    @Resource("api")
    class Api {

        @Resource("/business")
        class Business(val parent: Api = Api()) {

            @Resource("/healthcheck")
            class HealthCheck(val parent: Business = Business())

            @Resource("/{id}")
            class Id(val parent: Business = Business(), val id: Uuid)
        }
    }
}