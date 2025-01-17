package com.bookk.core.service.test

import com.book.core.service.installDocumentationPlugin
import com.book.core.service.installNegotiation
import com.bookk.core.test.runTest
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.resources.Resources
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

fun Application.installTestPlugins() {
    installDocumentationPlugin()
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

fun serverTest(body: suspend ApplicationTestBuilder.() -> Unit) {
    runTest {
        testApplication {
            body(this)
        }
    }
}