package com.book.business.microservice.route

import com.book.business.microservice.route.api.createBusiness
import io.ktor.server.routing.Routing


fun Routing.businessRoute() {
    createBusiness()
}