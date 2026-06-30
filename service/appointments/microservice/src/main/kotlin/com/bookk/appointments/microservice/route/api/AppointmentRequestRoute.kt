package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentOffer
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.api.operation.DeclineAppointmentRequest
import com.bookk.appointments.domain.api.operation.GetPendingAppointmentRequests
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.service.enity.respondWith
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.inject

fun Routing.requests() {
    authenticate {
        /**
         * Summary: Get pending appointment requests
         * Tag: appointment
         * Security: jwt
         */
        get<Api.Appointment.Requests> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getRequests by application.inject<GetPendingAppointmentRequests>()

            call.respondWith(getRequests(principal.userId, it.businessId))
        }.describe {
            responses {
               response(HttpStatusCode.OK.value) {
                   schema = jsonSchema<List<AppointmentRequest>>()
                   description = "List of appointment requests"
                   ContentType.Application.ProtoBuf()
               }
            }
        }

        /**
         * Summary: Create appointment request
         * Description: Create new appointment request
         * Tag: appointment
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.appointments.domain.api.entity.AppointmentOffer]
         * Response: 204 application/x-protobuf Request successfully created
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create appointment request errors<br>REQUEST_EXISTS (300001) Request for this time already exists<br>DATE_NOT_ALLOWED (300003) Request for this date not allowed<br>TIME_NOT_ALLOWED (300002) Request for this time not allowed<br>DATE_IN_PAST (300012) Request date is in the past
         */
        post<Api.Appointment.Request> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AppointmentOffer>()
            val createRequest by application.inject<CreateAppointmentRequest>()

            call.respondWith(
                createRequest(
                    userId = principal.userId,
                    offer = body
                )
            )
        }

        /**
         * Summary: Cancel appointment request
         * Description: Cancel appointment request with specific reason
         * Tag: appointment
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.appointments.domain.api.entity.AppointmentCancellation]
         * Response: 204 application/x-protobuf Appointment request canceled
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Cancel appointment request errors<br>REQUEST_ALREADY_DECLINED (300007) Appointment request already declined<br>REQUEST_ALREADY_APPROVED (300008) Appointment request already approved
         */
        post<Api.Appointment.RequestCancel> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AppointmentCancellation>()
            if (it.id != body.id) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            } else {
                val cancelRequest by application.inject<DeclineAppointmentRequest>()

                call.respondWith(
                    cancelRequest(
                        userId = principal.userId,
                        cancellation = body
                    )
                )
            }
        }
    }
}
