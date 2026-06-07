package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.operation.CancelAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.GetAppointments
import com.bookk.appointments.domain.api.operation.UpdateAppointment
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
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

@Serializable
internal class AppointmentRequestId(
    val requestId: Uuid,
)

fun Routing.appointment() {
    authenticate {
        put<Api.Appointment.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<Appointment>()
            if (it.id != body.id) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            } else {
                val updateAppointment by application.inject<UpdateAppointment>()

                call.respondWith(
                    updateAppointment(
                        userId = principal.userId,
                        appointment = body
                    )
                )
            }
        }.describe {
            summary = "Update appointment"
            description = "Update appointment entity(reschedule supported)"
            tag("appointment")
            requestBody {
                required = true
                schema = jsonSchema<Appointment>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Updated appointment entity"
                    schema = jsonSchema<Appointment>()
                    ContentType.Application.ProtoBuf()
                }
                HttpStatusCode.UnprocessableEntity {
                    description = buildString {
                        append(UpdateAppointment.Error.AppointmentForThisTimeExists().asServerError().toString())
                        append("\n\n")
                        append(UpdateAppointment.Error.RequestForThisDateNotAllowed().asServerError().toString())
                        append("\n\n")
                        append(UpdateAppointment.Error.RequestForThisTimeNotAllowed().asServerError().toString())
                    }
                    schema = jsonSchema<SimpleServerError>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }
        get<Api.Appointments> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getAppointments by application.inject<GetAppointments>()

            call.respondWith(getAppointments(principal.userId, it.businessId))
        }.describe {
            summary = "Get appointments"
            tag("appointment")
            responses {
                HttpStatusCode.OK {
                    description = "List of appointments"
                    schema = jsonSchema<List<Appointment>>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        post<Api.Appointment> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AppointmentRequestId>()
            val createAppointment by application.inject<CreateAppointment>()

            call.respondWith(
                createAppointment(
                    userId = principal.userId,
                    appointmentRequestId = body.requestId
                )
            )
        }.describe {
            summary = "Create appointment"
            description = "Create new appointment from request"
            tag("appointment")
            requestBody {
                required = true
                schema = jsonSchema<AppointmentRequestId>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Created appointment entity"
                    schema = jsonSchema<Appointment>()
                    ContentType.Application.ProtoBuf()
                }
                HttpStatusCode.UnprocessableEntity {
                    description = buildString {
                        append(CreateAppointment.Error.AppointmentForThisTimeExists().asServerError().toString())
                        append("\n\n")
                        append(CreateAppointment.Error.RequestForThisDateNotAllowed().asServerError().toString())
                        append("\n\n")
                        append(CreateAppointment.Error.RequestForThisTimeNotAllowed().asServerError().toString())
                    }
                    schema = jsonSchema<SimpleServerError>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        post<Api.Appointment.Cancel> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AppointmentCancellation>()
            if (it.id != body.id) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            } else {
                val cancelAppointment by application.inject<CancelAppointment>()

                call.respondWith(
                    cancelAppointment(
                        userId = principal.userId,
                        cancellation = body
                    )
                )
            }
        }.describe {
            summary = "Cancel appointment"
            description = "Cancel appointment with specific reason"
            tag("appointment")
            requestBody {
                required = true
                schema = jsonSchema<AppointmentCancellation>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.NoContent {
                    description = "Appointment canceled"
                }
                HttpStatusCode.UnprocessableEntity {
                    description = buildString {
                        append(CancelAppointment.Error.AlreadyCancelled().asServerError().toString())
                        append("\n\n")
                        append(CancelAppointment.Error.AlreadyCompleted().asServerError().toString())
                    }
                    schema = jsonSchema<SimpleServerError>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }
    }
}