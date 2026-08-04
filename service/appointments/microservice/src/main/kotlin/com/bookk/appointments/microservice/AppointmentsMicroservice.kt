package com.bookk.appointments.microservice

import com.bookk.appointments.data.di.appointmentsDataModule
import com.bookk.appointments.domain.api.operation.DeleteOutdatedRequests
import com.bookk.appointments.domain.api.operation.MarkAppointmentsCompleted
import com.bookk.appointments.domain.impl.di.appointmentsDomainModule
import com.bookk.appointments.microservice.route.appointmentsRoute
import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.data.eventstreaming.startEventHandling
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import com.bookk.server.business.client.di.businessClientModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import library.scheduler.Scheduler
import org.koin.dsl.module
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun appointmentsModule() = module {
    includes(
        appointmentsDomainModule(),
        appointmentsDataModule(),
        eventStreamingModule(),
        cacheModule(),
        businessClientModule("appointments")
    )
}

fun main() {
    startServer(diModules = listOf(appointmentsModule())) { app ->
        installNegotiation()
        app.installScheduler()
        startEventHandling()
        appointmentsRoute()
    }
}

fun Application.installScheduler() {
    install(Scheduler) {
        job("markAppointmentsAsCompleted", interval = 5.minutes) {
            get<MarkAppointmentsCompleted>().invoke().getOrThrow()
        }
        job("deleteOutdatedRequests", interval = 1.hours) {
            get<DeleteOutdatedRequests>().invoke().getOrThrow()
        }
    }
}
