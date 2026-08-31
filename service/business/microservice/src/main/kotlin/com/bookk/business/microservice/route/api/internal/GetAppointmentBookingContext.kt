package com.bookk.business.microservice.route.api.internal

import com.bookk.business.domain.api.appointment.entity.AppointmentBookingContextRequest
import com.bookk.business.domain.api.appointment.operation.GetAppointmentBookingContext
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.enity.respondWith
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getAppointmentBookingContext() {
    /**
     * Summary: Get appointment booking context
     * Description: Resolve the employee, client and services for a booking from their ids. Used by other services to build a verified appointment snapshot
     * Tag: internal
     * Body: application/x-protobuf [com.bookk.business.domain.api.appointment.entity.AppointmentBookingContextRequest]
     * Response: 200 application/x-protobuf [com.bookk.business.domain.api.appointment.entity.AppointmentBookingContext] Resolved employee, client and services
     * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Get appointment booking context errors<br>BUSINESS_EMPLOYEE_NOT_EXISTS (200024) Employee with this id is missing<br>BUSINESS_CLIENT_NOT_EXISTS (200006) Client with this id is missing
     * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Get appointment booking context errors<br>BUSINESS_QUOTE_SERVICE_NOT_FOUND (200013) One or more services not found<br>BUSINESS_QUOTE_EMPTY_SERVICE_LIST (200014) Service list must not be empty
     */
    post<Api.Internal.Business.Id.AppointmentBookingContext> { resource ->
        val body = call.receive<AppointmentBookingContextRequest>()
        val getAppointmentBookingContext by application.inject<GetAppointmentBookingContext>()

        call.respondWith(
            getAppointmentBookingContext(
                businessId = resource.parent.id,
                employeeId = body.employeeId,
                clientId = body.clientId,
                serviceIds = body.serviceIds
            )
        )
    }
}
