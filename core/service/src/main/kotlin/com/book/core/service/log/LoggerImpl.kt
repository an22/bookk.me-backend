package com.book.core.service.log

import com.bookk.core.Logger
import io.ktor.util.logging.*

class LoggerImpl : Logger {

    private val consoleLogger = KtorSimpleLogger("bookk.me")

    override fun error(throwable: Throwable, message: String?) {
        consoleLogger.error(message, throwable)
    }

    override fun info(message: String) {
        consoleLogger.info(message)
    }

    override fun debug(message: String) {
        consoleLogger.debug(message)
    }
}