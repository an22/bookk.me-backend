package com.bookk.core

interface Logger {
    fun error(throwable: Throwable, message: String? = null)
    fun info(message: String)
    fun debug(message: String)
}