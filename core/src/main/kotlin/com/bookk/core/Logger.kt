package com.bookk.core

interface Logger {
    fun d(message: String)
    fun e(throwable: Throwable)
    fun e(message: String, throwable: Throwable)
    fun i(message: String)
}