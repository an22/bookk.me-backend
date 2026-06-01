package com.bookk.auth.microservice

import com.bookk.auth.data.di.authDataModule
import com.bookk.auth.domain.impl.di.authDomainModule
import com.bookk.auth.microservice.route.authRoute
import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.service.auth.JwtConfig
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
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
        installNegotiation()
        authRoute()
    }
}
