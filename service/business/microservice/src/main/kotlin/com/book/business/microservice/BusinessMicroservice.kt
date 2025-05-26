package com.book.business.microservice

import com.book.business.data.di.businessDataModule
import com.book.business.domain.impl.di.businessDomainModule
import com.book.business.microservice.route.businessRoute
import com.book.core.data.cache.impl.di.cacheModule
import com.book.core.data.eventstreaming.di.eventStreamingModule
import com.book.core.service.installNegotiation
import com.book.core.service.startServer
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.dsl.module

fun businessModule() = module {
    includes(
        businessDomainModule(),
        businessDataModule(),
        cacheModule(),
        eventStreamingModule()
    )
}

fun main() {
    startServer(diModules = listOf(businessModule())) {
        setupRouting()
    }
}

private fun Application.setupRouting() {
    routing {
        installNegotiation()
        businessRoute()
    }
}