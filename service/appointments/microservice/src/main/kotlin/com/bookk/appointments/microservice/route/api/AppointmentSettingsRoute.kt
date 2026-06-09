package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.operation.EditSettings
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.put
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Routing.settings() {
    authenticate {
        /**
         * Summary: Update settings
         * Description: Update appointment settings
         * Tag: appointment
         * Security: jwt
         * RequestBody: application/x-protobuf [com.bookk.appointments.domain.api.entity.AppointmentSettings]
         * Response: 200 application/x-protobuf [com.bookk.appointments.domain.api.entity.AppointmentSettings] Updated settings entity
         */
        put<Api.Appointment.Settings> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AppointmentSettings>()
            val editSettings by application.inject<EditSettings>()

            call.respondWith(
                editSettings(
                    userId = principal.userId,
                    settings = body
                )
            )
        }
    }
}
