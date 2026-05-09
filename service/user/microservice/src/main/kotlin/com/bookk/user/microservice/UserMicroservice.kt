package com.bookk.user.microservice

import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.service.ServiceConfig
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import com.bookk.user.data.di.userDataModule
import com.bookk.user.domain.impl.di.userDomainModule
import com.bookk.user.microservice.route.userRoute
import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import org.koin.dsl.module
import org.koin.ktor.ext.getKoin

fun userModule() = module {
    includes(
        userDomainModule(),
        userDataModule(),
        cacheModule(),
        eventStreamingModule()
    )
}

fun main() {
    startServer(
        config = ServiceConfig(
            title = "UserMicroservice",
            version = "0.0.1",
            root = "api/user"
        ),
        diModules = listOf(userModule())
    ) { application ->
        application.setupEventHandling()
        setupRouting()
    }
}

private fun Application.setupEventHandling() {
    getKoin().getAll<EventHandler>().forEach {
        it.start(this)
    }
}

private fun Routing.setupRouting() {
    installNegotiation()
    userRoute()
}