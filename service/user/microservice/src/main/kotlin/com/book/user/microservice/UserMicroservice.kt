package com.book.user.microservice

import com.book.core.data.cache.impl.di.cacheModule
import com.book.core.data.eventstreaming.di.eventStreamingModule
import com.book.core.service.installNegotiation
import com.book.core.service.startServer
import com.book.user.data.di.userDataModule
import com.book.user.domain.api.event.UserEventHandler
import com.book.user.domain.impl.di.userDomainModule
import com.book.user.microservice.route.userRoute
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.dsl.module
import org.koin.ktor.ext.get

fun userModule() = module {
    includes(
        userDomainModule(),
        userDataModule(),
        cacheModule(),
        eventStreamingModule()
    )
}

fun main() {
    startServer(diModules = listOf(userModule())) {
        setupEventHandling()
        setupRouting()
    }
}

private fun Application.setupEventHandling() {
    val eventHandler: UserEventHandler = get()
    eventHandler.start(this)
}

private fun Application.setupRouting() {
    routing {
        installNegotiation()
        userRoute()
    }
}