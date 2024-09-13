package com.book.user.microservice

import com.book.core.data.cache.impl.di.cacheModule
import com.book.core.data.eventstreaming.di.eventStreamingModule
import com.book.core.service.installNegotiation
import com.book.core.service.log.LoggerImpl
import com.book.core.service.startServer
import com.book.user.data.di.userDataModule
import com.book.user.domain.impl.di.DIQualifier
import com.book.user.domain.impl.di.userDomainModule
import com.book.user.microservice.route.userRoute
import com.bookk.core.Logger
import io.ktor.server.routing.*
import org.koin.dsl.module

fun userModule() = module {
    single<Logger>(DIQualifier.USER) { LoggerImpl() }
    includes(
        userDomainModule(),
        userDataModule(DIQualifier.USER),
        cacheModule(DIQualifier.USER),
        eventStreamingModule(DIQualifier.USER)
    )
}

fun main() {
    startServer(diModules = listOf(userModule())) {
        routing {
            installNegotiation()
            userRoute()
        }
    }
}