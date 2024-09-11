package com.bookk.server

import com.book.auth.microservice.authModule
import com.book.auth.microservice.route.authRoute
import com.book.core.service.installNegotiation
import com.book.core.service.startServer
import com.book.user.microservice.route.userRoute
import com.book.user.microservice.userModule
import io.ktor.server.routing.*


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