package com.bookk.core.data.eventstreaming

import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import org.koin.ktor.ext.getKoin

interface EventHandler {
    fun start(scope: CoroutineScope)
}

fun Routing.startEventHandling() {
    application.getKoin().getAll<EventHandler>().forEach {
        it.start(application + Dispatchers.Default)
    }
}