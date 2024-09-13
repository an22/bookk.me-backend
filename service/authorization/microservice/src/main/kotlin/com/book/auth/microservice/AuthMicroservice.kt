package com.book.auth.microservice

import com.book.auth.data.di.authDataModule
import com.book.auth.domain.impl.di.DIQualifier
import com.book.auth.domain.impl.di.authDomainModule
import com.book.auth.microservice.route.authRoute
import com.book.core.data.cache.impl.di.cacheModule
import com.book.core.data.eventstreaming.di.eventStreamingModule
import com.book.core.service.auth.JwtConfig
import com.book.core.service.installNegotiation
import com.book.core.service.log.LoggerImpl
import com.book.core.service.startServer
import com.bookk.core.Logger
import io.ktor.server.routing.*
import org.koin.dsl.module

fun authModule() = module {
    single(DIQualifier.DOMAIN_NAME) { System.getenv(DIQualifier.DOMAIN_NAME.value) }
    single { JwtConfig.createPrivateKeyProvider() }
    single<Logger>(DIQualifier.AUTH) { LoggerImpl() }
    includes(
        authDomainModule(),
        authDataModule(DIQualifier.AUTH),
        eventStreamingModule(DIQualifier.AUTH),
        cacheModule(DIQualifier.AUTH)
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
