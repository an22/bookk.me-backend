package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.api.operation.IsAppointmentsEnabled
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.service.enity.respondWith
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
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
         * Body: application/x-protobuf [com.bookk.appointments.domain.api.entity.BusinessSnapshot]
         * Response: 204 application/x-protobuf Created appointment entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Appointment errors<br>PLUGIN_ALREADY_ENABLED (300009) Appointment plugin already enabled
         */
        post<Api.Appointment.Enabled> {
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

        /**
         * Summary: Check if enabled
         * Description: Check whether appointments plugin is enabled or not
         * Tag: appointment
         * Security: jwt
         * Response: 200 application/x-protobuf [kotlin.Boolean] true or false
         */
        get<Api.Appointment.Enabled> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val isEnabled by application.inject<IsAppointmentsEnabled>()

            call.respondWith(
                isEnabled(
                    userId = principal.userId,
                    businessId = it.businessId
                )
            )
        }
    }
}
