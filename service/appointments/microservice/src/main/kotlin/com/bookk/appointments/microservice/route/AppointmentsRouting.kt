package com.bookk.appointments.microservice.route

import io.ktor.resources.Resource
import kotlin.uuid.Uuid

object AppointmentsRouting {
    @Resource("api")
    class Api {

        @Resource("/appointment")
        class Appointment(val parent: Api = Api()) {

            @Resource("/{id}")
            class Id(val parent: Appointment = Appointment(), val id: Uuid)

            @Resource("/healthcheck")
            class HealthCheck(val parent: Appointment = Appointment())

            @Resource("/request")
            class Request(val parent: Appointment = Appointment())

            @Resource("/request/{businessId}")
            class Requests(val parent: Appointment = Appointment(), val businessId: Uuid)

            @Resource("/settings/{businessId}")
            class Settings(val parent: Appointment = Appointment(), val businessId: Uuid)
        }

        @Resource("/appointments")
        class Appointments(val parent: Api = Api(), val businessId: Uuid)
    }

}