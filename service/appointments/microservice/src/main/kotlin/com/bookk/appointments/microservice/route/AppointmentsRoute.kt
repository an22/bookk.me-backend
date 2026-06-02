package com.bookk.appointments.microservice.route

import com.bookk.appointments.microservice.route.api.createAppointment
import com.bookk.appointments.microservice.route.api.healthCheck
import io.ktor.server.routing.Routing

fun Routing.appointmentsRoute() {
    healthCheck()
    createAppointment()
}