package com.book.business.microservice.route

import com.book.business.microservice.route.api.createBusiness
import com.book.business.microservice.route.api.healthCheck
import io.ktor.server.routing.Routing


fun Routing.businessRoute() {
    healthCheck()
    createBusiness()
}