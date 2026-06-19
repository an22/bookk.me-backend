package com.bookk.auth.microservice

import com.bookk.auth.data.di.authDataModule
import com.bookk.auth.domain.api.token.operation.RotateSigningKeys
import com.bookk.auth.domain.impl.di.authDomainModule
import com.bookk.auth.microservice.route.authRoute
import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import io.ktor.server.application.Application
import io.ktor.server.application.install
import library.scheduler.Scheduler
import org.koin.dsl.module
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.days

fun authModule() = module {
    includes(
        authDomainModule(),
        authDataModule(),
        eventStreamingModule(),
        cacheModule()
    )
}

fun main() {
    startServer(diModules = listOf(authModule())) { app ->
        installNegotiation()
        app.installScheduler()
        authRoute()
    }
}

fun Application.installScheduler() {
    install(Scheduler) {
        job("rotateSigningKeys", interval = 1.days) {
            get<RotateSigningKeys>().invoke().getOrThrow()
        }
    }
}
