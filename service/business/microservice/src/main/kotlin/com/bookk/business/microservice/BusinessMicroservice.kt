package com.bookk.business.microservice

import com.bookk.business.data.di.businessDataModule
import com.bookk.business.domain.impl.di.businessDomainModule
import com.bookk.business.microservice.route.businessRoute
import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.data.eventstreaming.startEventHandling
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import io.ktor.server.application.Application
import io.ktor.server.application.install
import library.scheduler.Scheduler
import library.signing.RotateSigningKeys
import org.koin.dsl.module
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.days

fun businessModule() = module {
    includes(
        businessDomainModule(),
        businessDataModule(),
        cacheModule(),
        eventStreamingModule()
    )
}

fun main() {
    startServer(diModules = listOf(businessModule())) { app ->
        installNegotiation()
        startEventHandling()
        app.installScheduler()
        businessRoute()
    }
}

fun Application.installScheduler() {
    install(Scheduler) {
        job("rotateSigningKeys", interval = 7.days) {
            get<RotateSigningKeys>().invoke(retireInterval = 7.days).getOrThrow()
        }
    }
}