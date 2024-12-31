package com.book.auth.microservice

import com.book.auth.data.di.authDataModule
import com.book.auth.domain.impl.di.authDomainModule
import com.book.auth.microservice.route.authRoute
import com.book.core.data.cache.impl.di.cacheModule
import com.book.core.data.eventstreaming.di.eventStreamingModule
import com.book.core.service.auth.JwtConfig
import com.book.core.service.installNegotiation
import com.book.core.service.startServer
import io.ktor.server.routing.routing
import org.koin.dsl.module

fun authModule() = module {
    single { JwtConfig.createPrivateKeyProvider() }
    includes(
        authDomainModule(),
        authDataModule(),
        eventStreamingModule(),
        cacheModule()
    )
}

fun main() {
    startServer(diModules = listOf(authModule())) {
        routing {
            installNegotiation()
            authRoute()
        }
    }
}
