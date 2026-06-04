package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.domain.entity.asServerError
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.inject

fun Routing.requests() {
    authenticate {
        post<Api.Appointments.Request> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AppointmentRequest>()
            val createRequest by application.inject<CreateAppointmentRequest>()

            call.respondWith(
                createRequest(
                    userId = principal.userId,
                    request = body
                )
            )
        }.describe {
            summary = "Create appointment request"
            description = "Create new appointment request"
            tag("appointment")
            requestBody {
                required = true
                schema = jsonSchema<AppointmentRequest>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.NoContent {
                    description = "Request successfully created"
                }
                HttpStatusCode.UnprocessableEntity {
                    description = buildString {
                        append(CreateAppointmentRequest.Error.RequestForThisTimeExists().asServerError().toString())
                        append("\n\n")
                        append(CreateAppointmentRequest.Error.RequestForThisDateNotAllowed().asServerError().toString())
                        append("\n\n")
                        append(CreateAppointmentRequest.Error.RequestForThisTimeNotAllowed().asServerError().toString())
                    }
                    schema = jsonSchema<SimpleServerError>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }
    }
}