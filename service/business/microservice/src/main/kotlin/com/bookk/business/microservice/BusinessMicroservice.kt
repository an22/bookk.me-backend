package com.bookk.business.microservice

import com.bookk.business.data.di.businessDataModule
import com.bookk.business.domain.impl.di.businessDomainModule
import com.bookk.business.microservice.route.businessRoute
import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.data.eventstreaming.startEventHandling
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
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
        installNegotiation()
        startEventHandling()
        businessRoute()
    }
}