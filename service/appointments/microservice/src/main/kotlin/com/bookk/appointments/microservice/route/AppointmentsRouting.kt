package com.bookk.appointments.microservice.route

import io.ktor.resources.Resource
import kotlin.uuid.Uuid

object AppointmentsRouting {
    @Resource("api")
    class Api {

        @Resource("/appointments")
        class Appointments(val parent: Api = Api()) {
            @Resource("/healthcheck")
            class HealthCheck(val parent: Appointments = Appointments())

            @Resource("/settings/{businessId}")
            class Settings(val parent: Appointments = Appointments(), val businessId: Uuid)
        }
    }

}