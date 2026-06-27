package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.operation.CancelAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.GetAppointmentHistory
import com.bookk.appointments.domain.api.operation.GetAppointmentsForDate
import com.bookk.appointments.domain.api.operation.UpdateAppointment
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
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
        /**
         * Summary: Update appointment
         * Description: Update appointment entity(reschedule supported)
         * Tag: appointment
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.appointments.domain.api.entity.Appointment]
         * Response: 200 application/x-protobuf [com.bookk.appointments.domain.api.entity.Appointment] Updated appointment entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Update appointment errors<br>APPOINTMENT_EXISTS (300004) Appointment for this time already exists<br>DATE_NOT_ALLOWED (300003) Request for this date not allowed<br>TIME_NOT_ALLOWED (300002) Request for this time not allowed<br>DATE_IN_PAST (300012) Appointment date is in the past
         */
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
        }
        /**
         * Summary: Get appointments for specific date
         * Tag: appointment
         * Security: jwt
         */
        get<Api.Appointments> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getAppointments by application.inject<GetAppointmentsForDate>()

            call.respondWith(getAppointments(principal.userId, it.businessId, it.date))
        }.describe {
            responses {
                response(HttpStatusCode.OK.value) {
                    schema = jsonSchema<List<Appointment>>()
                    description = "List of appointments"
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        /**
         * Summary: Get appointments history
         * Description: Get paginated appointments history, optionally filtered by a query matching client name or service names
         * Tag: appointment
         * Security: jwt
         * Response: 200 application/x-protobuf [com.bookk.appointments.domain.api.entity.AppointmentPagination] List of appointments
         */
        get<Api.AppointmentHistory> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getHistory by application.inject<GetAppointmentHistory>()

            call.respondWith(
                getHistory(
                    userId = principal.userId,
                    businessId = it.businessId,
                    limit = it.limit,
                    offset = it.offset,
                    query = it.query
                )
            )
        }

        /**
         * Summary: Create appointment
         * Description: Create new appointment from request
         * Tag: appointment
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.appointments.microservice.route.api.AppointmentRequestId]
         * Response: 200 application/x-protobuf [com.bookk.appointments.domain.api.entity.Appointment] Created appointment entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create appointment errors<br>APPOINTMENT_EXISTS (300004) Appointment for this time already exists<br>DATE_NOT_ALLOWED (300003) Request for this date not allowed<br>TIME_NOT_ALLOWED (300002) Request for this time not allowed<br>DATE_IN_PAST (300012) Appointment date is in the past
         */
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
        }

        /**
         * Summary: Create appointment
         * Description: Create new appointment from request
         * Tag: appointment
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.appointments.domain.api.entity.Appointment]
         * Response: 200 application/x-protobuf [com.bookk.appointments.domain.api.entity.Appointment] Created appointment entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create appointment errors<br>APPOINTMENT_EXISTS (300004) Appointment for this time already exists<br>DATE_NOT_ALLOWED (300003) Request for this date not allowed<br>TIME_NOT_ALLOWED (300002) Request for this time not allowed<br>DATE_IN_PAST (300012) Appointment date is in the past
         */
        post<Api.Appointment.Instant> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<Appointment>()
            val createAppointment by application.inject<CreateAppointment>()

            call.respondWith(
                createAppointment(
                    userId = principal.userId,
                    appointment = body
                )
            )
        }

        /**
         * Summary: Cancel appointment
         * Description: Cancel appointment with specific reason
         * Tag: appointment
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.appointments.domain.api.entity.AppointmentCancellation]
         * Response: 200 application/x-protobuf [com.bookk.appointments.domain.api.entity.Appointment] Canceled appointment
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Cancel appointment errors<br>ALREADY_CANCELLED (300005) Appointment already canceled<br>ALREADY_COMPLETED (300006) Appointment already completed
         */
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
        }
    }
}
