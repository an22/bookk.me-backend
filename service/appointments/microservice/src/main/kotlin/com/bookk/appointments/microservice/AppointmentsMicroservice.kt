package com.bookk.appointments.microservice

import com.bookk.appointments.data.di.appointmentsDataModule
import com.bookk.appointments.domain.impl.di.appointmentsDomainModule
import com.bookk.appointments.microservice.route.appointmentsRoute
import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.service.auth.JwtConfig
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import org.koin.dsl.module

fun appointmentsModule() = module {
    single { JwtConfig.createPrivateKeyProvider() }
    includes(
        appointmentsDomainModule(),
        appointmentsDataModule(),
        eventStreamingModule(),
        cacheModule()
    )
}

fun main() {
    startServer(diModules = listOf(appointmentsModule())) {
        installNegotiation()
        appointmentsRoute()
    }
}
