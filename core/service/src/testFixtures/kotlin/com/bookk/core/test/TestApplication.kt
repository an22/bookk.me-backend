package com.bookk.core.test

import com.book.core.service.installDocumentationPlugin
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.resources.Resources

fun Application.installTestPlugins() {
    installDocumentationPlugin()
    install(CallLogging)
    install(Resources)
}