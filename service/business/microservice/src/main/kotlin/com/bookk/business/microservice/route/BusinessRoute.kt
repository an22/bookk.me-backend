package com.bookk.business.microservice.route

import com.bookk.business.microservice.route.api.businessCrud
import com.bookk.business.microservice.route.api.clientCrud
import com.bookk.business.microservice.route.api.healthCheck
import io.ktor.server.routing.Routing


fun Routing.businessRoute() {
    healthCheck()
    businessCrud()
    clientCrud()
}