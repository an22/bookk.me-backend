package com.bookk.business.microservice.route

import com.bookk.business.microservice.route.api.businessCrud
import com.bookk.business.microservice.route.api.clientCrud
import com.bookk.business.microservice.route.api.healthCheck
import com.bookk.business.microservice.route.api.serviceCrud
import com.bookk.business.microservice.route.api.serviceGroupCrud
import io.ktor.server.routing.Routing


fun Routing.businessRoute() {
    healthCheck()
    businessCrud()
    clientCrud()
    serviceCrud()
    serviceGroupCrud()
}