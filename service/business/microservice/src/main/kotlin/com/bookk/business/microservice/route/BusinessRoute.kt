package com.bookk.business.microservice.route

import com.bookk.business.microservice.route.api.businessCrud
import com.bookk.business.microservice.route.api.clientCrud
import com.bookk.business.microservice.route.api.employeeInvitationCrud
import com.bookk.business.microservice.route.api.healthCheck
import com.bookk.business.microservice.route.api.internal.getBusinessById
import com.bookk.business.microservice.route.api.internal.getBusinessPermission
import com.bookk.business.microservice.route.api.quote
import com.bookk.business.microservice.route.api.serviceCrud
import com.bookk.business.microservice.route.api.serviceGroupCrud
import io.ktor.server.routing.Routing
import library.signing.route.jwks


fun Routing.businessRoute() {
    healthCheck()
    jwks()
    businessCrud()
    getBusinessById()
    getBusinessPermission()
    clientCrud()
    serviceCrud()
    serviceGroupCrud()
    quote()
    employeeInvitationCrud()
}