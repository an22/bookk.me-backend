package com.bookk.appointments.microservice.route.api

import com.bookk.appointments.domain.api.entity.AppointmentCancellation
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.api.operation.DeclineAppointmentRequest
import com.bookk.appointments.domain.api.operation.GetAppointmentRequests
import com.bookk.appointments.microservice.route.AppointmentsRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
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

fun Routing.requests() {
    authenticate {
        /**
         * Summary: Get appointment requests
         * Tag: appointment
         * Security: jwt
         * Response: 200 application/x-protobuf [kotlin.collections.List<com.bookk.appointments.domain.api.entity.AppointmentRequest>] List of appointment requests
         */
        get<Api.Appointment.Requests> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getRequests by application.inject<GetAppointmentRequests>()

            call.respondWith(getRequests(principal.userId, it.businessId))
        }

        /**
         * Summary: Create appointment request
         * Description: Create new appointment request
         * Tag: appointment
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.appointments.domain.api.entity.AppointmentRequest]
         * Response: 204 application/x-protobuf Request successfully created
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create appointment request errors<br>REQUEST_EXISTS (Code 300001) Request for this time already exists<br>DATE_NOT_ALLOWED (Code 300003) Request for this date not allowed<br>TIME_NOT_ALLOWED (Code 300002) Request for this time not allowed
         */
        post<Api.Appointment.Request> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<AppointmentRequest>()
            val createRequest by application.inject<CreateAppointmentRequest>()

            call.respondWith(
                createRequest(
                    userId = principal.userId,
                    request = body
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
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Cancel appointment request errors<br>REQUEST_ALREADY_DECLINED (Code 300007) Appointment request already declined<br>REQUEST_ALREADY_APPROVED (Code 300008) Appointment request already approved
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
