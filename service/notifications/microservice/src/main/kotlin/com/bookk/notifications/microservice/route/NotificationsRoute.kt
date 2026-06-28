package com.bookk.notifications.microservice.route

import com.bookk.notifications.microservice.route.api.healthCheck
import io.ktor.server.routing.Routing

fun Routing.notificationsRoute() {
    healthCheck()
}
