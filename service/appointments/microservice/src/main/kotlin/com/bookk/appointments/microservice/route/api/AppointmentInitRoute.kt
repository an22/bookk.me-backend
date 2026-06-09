package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Routing.appointmentInit() {
    authenticate {
        /**
         * Summary: Enable appointments
         * Description: Enable appointments for business
         * Tag: appointment
         * Security: jwt
         * RequestBody: application/x-protobuf [com.bookk.appointments.domain.api.entity.BusinessSnapshot]
         * Response: 204 application/x-protobuf Created appointment entity
         */
        post<Api.Appointment.Enable> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<BusinessSnapshot>()
            if (it.businessId != body.id) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            } else {
                val enableAppointments by application.inject<EnableAppointmentsForBusiness>()

                call.respondWith(
                    enableAppointments(
                        userId = principal.userId,
                        snapshot = body
                    )
                )
            }
        }
    }
}
