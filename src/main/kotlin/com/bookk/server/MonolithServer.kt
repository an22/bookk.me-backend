package com.bookk.server

import com.bookk.auth.microservice.authModule
import com.bookk.auth.microservice.route.authRoute
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import com.bookk.user.microservice.route.userRoute
import com.bookk.user.microservice.userModule
import io.ktor.server.routing.routing


private fun diModules() = listOf(
    authModule(),
    userModule()
)

fun main() {
    startServer(
        diModules = diModules()
    ) {
        routing {
            installNegotiation()
            authRoute()
            userRoute()
        }
    }
}