package com.bookk.core.service.test

import com.bookk.core.service.installNegotiation
import com.bookk.core.test.TestHolder
import com.bookk.core.test.runIntegrationTest
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.resources.Resources
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import kotlin.time.Duration.Companion.seconds

fun Application.installTestPlugins() {
    install(Resources)
}

fun ApplicationTestBuilder.setupApplication(
    extension: (Application.() -> Unit)? = null,
    diModule: Module,
    routeUnderTest: Routing.() -> Unit
) {
    application {
        installTestPlugins()
        install(Koin) {
            modules(listOf(diModule))
        }
        extension?.invoke(this)
        routing {
            installNegotiation()
            routeUnderTest()
        }
    }
}

private val COLD_START_TOLERANT_TIMEOUT = 30.seconds

fun routeTest(body: suspend ApplicationTestBuilder.() -> Unit) {
    val context = TestHolder()
    runIntegrationTest(context, timeout = COLD_START_TOLERANT_TIMEOUT) {
        testApplication(context) {
            body(this)
        }
    }
}