package com.book.core.domain.entity

sealed class Error(cause: Throwable) : Throwable(cause) {
    class DatabaseError(val text: String, cause: Throwable) : Error(cause)
    class UnknownError(cause: Throwable) : Error(cause)
}