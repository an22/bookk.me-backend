package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.operation.EditSettings
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.put
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.inject

fun Routing.settings() {
    authenticate {
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
        }.describe {
            summary = "Update settings"
            description = "Update appointment settings"
            tag("appointment")
            requestBody {
                required = true
                schema = jsonSchema<AppointmentSettings>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Updated settings entity"
                    schema = jsonSchema<AppointmentSettings>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }
    }
}