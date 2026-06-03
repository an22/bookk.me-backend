package com.bookk.user.microservice

import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.data.eventstreaming.startEventHandling
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import com.bookk.user.data.di.userDataModule
import com.bookk.user.domain.impl.di.userDomainModule
import com.bookk.user.microservice.route.userRoute
import io.ktor.server.routing.Routing
import org.koin.dsl.module

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
        startEventHandling()
        setupRouting()
    }
}

private fun Routing.setupRouting() {
    installNegotiation()
    userRoute()
}